#!/usr/bin/env python
"""
        OPENPIXI SGE/SLURM BATCH SCRIPT

    This script creates a batch of yaml files and job files based on the template files and submits them using
    qsub or sbatch.

    Example usage:
    Parse the input file 'pancakewidths', create yaml and qjob/slrm files as specified and submit them.
        python openpixi_batch.py -cs -i pancakewidths

    Under linux one can call the python script directly:
        ./openpixi_batch.py -cs -i pancakewidth

    Override the job manager.
        python openpixi_batch.py -cs -i pancakewidth -j SGE

    Override the output directory.
        python openpixi_batch.py -cs -i pancakewidths -o pancakewidths_files_alternative_location/

    Parse the input file 'coupling' and create yaml and qjob files in 'pancake_widths/' (without submitting them).
        python openpixi_batch.py -c -i coupling -o coupling_files/

    Submit all *.qjob files in the 'jobs' folder.
        python openpixi_batch.py -s -j SGE -o jobs/

    Submit all *.slrm files in the 'jobs' folder.
        python openpixi_batch.py -s -j SLURM -o jobs/

    Options:
    -c or --create: create YAML and job files based on the input file path and write them to output path.
    -s or --submit: submit job files in the output path.
    -i INPUT or --input=INPUT: read yaml and job templates from INPUT
    -o OUTPUT or --output=OUTPUT: path where yaml and job are files are to be stored
    -j JOBMANAGER or --jobmanager=JOBMANAGER: specifies job manager to be used (only required when using -s without -c)

    NOTE: when script is called to create yaml and job files, all of the files in the output path are deleted.

    Some options for YAML/JOB templates:

    1) Defining the integer range
    The first line must define the integer range for the simulations.
    %range BEGIN END%

    Example:
    %range 1 10% for 10 simulations with integers 1, 2, .., 10.

    Note: Don't start counting at zero, SGE apparently does not like task ids which are zero.

    2) Define path to jar file
    After the first line include the path to the jar-file.

    Example:
    %jar pixi-0.6-SNAPSHOT.jar% uses the jar-file pixi-0.6-SNAPSHOT.jar in the same directory as the input file.

    3) Define job manager type
    Use %jobmanager SGE% for SGE (on the VSC2) or %jobmanager SLURM% for SLURM (on the VSC3).
    (This option can be overridden by the command line option '--jobmanager' or '-j')

    4) Define output path for temporary files
    Use %output begin%...pathname...%output end% to define the location of output files.
    Use %job_name% to refer to the input file name.

    5) Using integer from range
    Use %i% to insert the integer from the given range.

    Example:
    Use "output%i%.dat" to get output files "output0.dat", "output1.dat", ..., "output9.dat" given the range above.

    6) Float intervals
    Use %f BEGIN END% to define a linear range of floating point numbers.

    Example:
    %f 0.0 0.9% to get the values 0.0, 0.1, ... 0.9 using the integer range above.

    7) Begin and end of the integer range
    Use %i0% for the first integer in the range and %i1% for the last. This is used in the job template to specify
    the correct range for the array job.

    8) Evaluate arbitrary python expressions
    Use %eval ...% to evaluate arbitrary python expressions that can use the variables 'i', 'i0' and 'i1'.
    Use an %exec begin%...%exec end% python code block that is executed every time just before %eval ...%.
    %eval ...% can be used in the yaml template as well as in the job template.

"""

import re
import os
import errno
from optparse import OptionParser

job_managers = ["SGE", "SLURM"]


def main():
    """Main function of the script. Checks for given arguments and does whatever is to be done."""

    parser = OptionParser()
    parser.add_option("-c", "--create", action="store_true", dest="create", default=False,
                      help="create YAML and job files based on input file.")

    parser.add_option("-s", "--submit", action="store_true", dest="submit", default=False,
                      help="submit all *.qjob or *.slrm files in output path.")

    parser.add_option("-i", "--input", action="store", type="string", dest="input", metavar="INPUT",
                      help="path to input file (yaml and job template)")

    parser.add_option("-o", "--output", action="store", type="string", dest="output", metavar="OUTPUT",
                      help="output path (where yaml and job files will be stored)")

    parser.add_option("-j", "--jobmanager", action="store", type="string", dest="jobmanager", metavar="JOBMANAGER",
                      help="specifies the jobmanager used ('SGE' for qjob files or 'SLURM' for slrm files). Only "
                           "required when using -s without -c")

    (options, args) = parser.parse_args()

    input_path = options.input
    output_path = options.output
    jobmanager = options.jobmanager

    if options.create:
        if input_path is not None:
            conf_object = parse_template(input_path, output_path, jobmanager)
            if conf_object.o_path is not None:
                create_yaml_files(conf_object)
                create_jobfile(conf_object)

                # also submit job (job manager is specified in configuration object)
                if options.submit:
                    submit_jobs(conf_object.o_path, conf_object.job_manager)

            else:
                print("Error: Output path not defined.")
                exit(-1)
        else:
            print("Error: Input path not defined.")
            exit(-1)

    elif options.submit:
        if options.jobmanager is not None:
            if output_path is not None:
                submit_jobs(output_path, options.jobmanager)
            else:
                print("Error: Path to job files not defined.")
                exit(-1)
        else:
            print("Error: Job manager must be specified.")


def empty_path(path):
    """
    Empties the temporary path.
    This function is called before new yaml and qjob files are created.
    :param path: path to folder
    :return:
    """
    make_sure_path_exists(path)
    for f in os.listdir(path):
        p = os.path.join(path, f)
        if os.path.isfile(p):
            os.remove(p)


def parse_template(i_path, o_path, j_manager):
    """
    Parses template file and returns configuration object.
    :param i_path: path to template file
    :param o_path: intended output folder
    :return: configuration object
    """
    script_string = open(i_path, "r").read()

    # Create job name from input file name
    job_name = os.path.basename(i_path)

    # find and read integer range line (should be first line, but doesnt really matter.)
    (b, e) = re.search("%range.*%", script_string).span()
    (range_begin, range_end) = re.sub("%", "", re.sub("%range\s", "", script_string[b:e])).split(" ")
    int_range = range(int(range_begin), int(range_end) + 1)
    int_range_len = len(int_range)
    script_string = script_string.replace(script_string[b:e], "")

    # find and read jar path
    (b, e) = re.search("%jar\s.*%", script_string).span()
    jar_path = re.sub("%", "", re.sub("%jar\s", "", script_string[b:e]))

    # use jobmanager from command line or from file
    if j_manager:
        # if it exists, command line argument overrides setting in file
        job_manager = j_manager
    else:
        # find and read job manager type
        (b, e) = re.search("%jobmanager\s.*%", script_string).span()
        job_manager = re.sub("%", "", re.sub("%jobmanager\s", "", script_string[b:e]))

    if job_manager not in job_managers:
        print("Unknown job manager type '" + job_manager + "'. Use one of these: " + str(job_managers))
        exit(-1)

    # parse yaml and job template
    r1 = re.compile("%yaml begin%\n([\S\s]*)%yaml end%")
    r2 = re.compile("%job begin%\n([\S\s]*)%job end%")
    r3 = re.compile("%SGE job begin%\n([\S\s]*)%SGE job end%")
    r4 = re.compile("%SLURM job begin%\n([\S\s]*)%SLURM job end%")
    if not r1.search(script_string):
        print("Found no proper YAML template in " + i_path)
        exit(-1)
    if r2.search(script_string):
        job_template_string = r2.search(script_string).group(1)
    else:
        job_template_string = ""
    if r3.search(script_string):
        sge_job_template_string = r3.search(script_string).group(1)
    else:
        sge_job_template_string = ""
    if r4.search(script_string):
        slurm_job_template_string = r4.search(script_string).group(1)
    else:
        slurm_job_template_string = ""

    yaml_template_string = r1.search(script_string).group(1)

    if (        (not job_template_string)
            and (not sge_job_template_string)
            and (not slurm_job_template_string)):
        print("Found no proper job template in " + i_path)
        exit(-1)

    if (        (job_manager == "SGE")
            and (not job_template_string)
            and (not sge_job_template_string)):
        print("Found no proper job template for SGE in " + i_path)
        exit(-1)

    if (        (job_manager == "SLURM")
            and (not job_template_string)
            and (not slurm_job_template_string)):
        print("Found no proper job template for SLURM in " + i_path)
        exit(-1)

    # parse output file
    if o_path is not None:
        # Command line option overrides option in file
        parse_o_path = o_path
    else:
        r3 = re.compile("%output begin%([\S\s]*)%output end%")
        if r3.search(script_string):
            parse_o_path = r3.search(script_string).group(1)
            parse_o_path = parse_o_path.strip()
            parse_o_path = parse_o_path.replace("%job_name%", job_name)
        else:
            parse_o_path = None

    # find float ranges
    i = 0
    float_ranges_limits = []
    for float_line in re.finditer("%f\s[^%]*%", yaml_template_string):
        current_string = float_line.group()
        yaml_template_string = yaml_template_string.replace(current_string, "%f" + str(i) + "%")
        (float_begin, float_end) = re.sub("%", "", re.sub("%f\s", "", current_string)).split(" ")
        float_ranges_limits.append((float(float_begin), float(float_end)))
        i += 1

    float_ranges = []
    for fl in float_ranges_limits:
        b = fl[0]
        e = fl[1]
        float_ranges.append(frange(b, e, int_range_len))

    # parse exec object
    r1 = re.compile("%exec begin%\n([\S\s]*)%exec end%")
    if r1.search(script_string):
        exec_string = r1.search(script_string).group(1)
    else:
        exec_string = ""

    conf_object = Object()
    conf_object.job_name = job_name
    conf_object.i_path = i_path
    conf_object.o_path = parse_o_path
    conf_object.job_manager = job_manager
    conf_object.jar_path = jar_path
    conf_object.yaml_template_string = yaml_template_string
    conf_object.job_template_string = job_template_string
    conf_object.sge_job_template_string = sge_job_template_string
    conf_object.slurm_job_template_string = slurm_job_template_string
    conf_object.int_range = int_range
    conf_object.i0 = range_begin
    conf_object.i1 = range_end
    conf_object.float_ranges = float_ranges
    conf_object.exec_string = exec_string

    return conf_object


def submit_jobs(o_path, job_manager):
    """
    Submits the job file according to the job manager type.
    :param o_path: path to folder with job file(s)
    :param job_manager: type of job manager (either SGE or SLURM)
    :return:
    """
    if job_manager not in job_managers:
        print("Unknown job manager type " + job_manager + ". Use one of these: " + str(job_managers))
        exit(-1)
    else:
        if job_manager == "SGE":
            submit_sge_jobs(o_path)
        elif job_manager == "SLURM":
            submit_slurm_jobs(o_path)


def submit_sge_jobs(o_path):
    """
    Submits all *.qjob files in the given path.
    :param o_path: path to output files
    :return:
    """
    from subprocess import call
    if os.path.exists(o_path):
        qjob_files = []
        for f in os.listdir(o_path):
            if f.endswith(".qjob"):
                qjob_files.append(os.path.join(o_path, f))
        for f in qjob_files:
            try:
                print("Submitting " + f)
                call(["qsub", f])
            except OSError:
                print("Error: Can not call qsub command. Are you on a cluster?")
                exit(-1)


def submit_slurm_jobs(o_path):
    """
    Submits all *.slrm files in the given path.
    :param o_path: path to output files
    :return:
    """
    from subprocess import call
    if os.path.exists(o_path):
        qjob_files = []
        for f in os.listdir(o_path):
            if f.endswith(".slrm"):
                qjob_files.append(os.path.join(o_path, f))
        for f in qjob_files:
            try:
                print("Submitting " + f)
                call(["sbatch", f])
            except OSError:
                print("Error: Can not call sbatch command. Are you on a cluster?")
                exit(-1)


def create_yaml_files(conf_object):
    """
    Creates yaml files based on given configuration object.
    :param conf_object:
    :return:
    """
    # generate yaml files in output folder
    empty_path(conf_object.o_path)
    make_sure_path_exists(conf_object.o_path)
    c = 0
    yaml_files = []
    for i in conf_object.int_range:
        file_name = "tmp" + str(i) + ".yaml"
        path = os.path.join(conf_object.o_path, file_name)
        file_object = open(path, "w")
        yaml_files.append(file_name)
        current_yaml_string = str(conf_object.yaml_template_string)

        # replace all integers
        current_yaml_string = current_yaml_string.replace("%i%", str(i))

        # replace all float ranges
        float_index = 0
        for fr in conf_object.float_ranges:
            value = fr[c]
            current_yaml_string = current_yaml_string.replace("%f" + str(float_index) + "%", str(value))
            float_index += 1
        c += 1

        # replace all eval objects
        current_yaml_string = replace_eval(current_yaml_string, conf_object, i)

        # replace job name
        current_yaml_string = current_yaml_string.replace("%job_name%", conf_object.job_name)

        # write finished yaml string to file.
        file_object.write(current_yaml_string)
        file_object.close()


def create_jobfile(conf_object):
    """
    Creates job file based on a given configuration object.
    :param conf_object:
    :return:
    """
    # create SGE/SLURM job files for each yaml file based on template
    if conf_object.job_manager == "SLURM":
        input_file_name = "tmp$SLURM_ARRAY_TASK_ID.yaml"
        job_file_name = "openpixi_batch.slrm"
        job_string = str(conf_object.slurm_job_template_string)
    elif conf_object.job_manager == "SGE":
        input_file_name = "tmp$SGE_TASK_ID.yaml"
        job_file_name = "openpixi_batch.qjob"
        job_string = str(conf_object.sge_job_template_string)
    else:
        input_file_name = ""
        job_file_name = ""
        job_string = str(conf_object.job_template_string)

    # if no specific [sge/slurm]_job_template has been specified, use default one:
    if not job_string:
        job_string = str(conf_object.job_template_string)

    path = os.path.join(conf_object.o_path, job_file_name)
    job_string = job_string.replace("%job_name%", conf_object.job_name)
    job_string = job_string.replace("%jar_path%", conf_object.jar_path)
    input_path = os.path.join(conf_object.o_path, input_file_name)
    job_string = job_string.replace("%input_path%", input_path)
    job_string = job_string.replace("%i0%", conf_object.i0)
    job_string = job_string.replace("%i1%", conf_object.i1)

    # replace all eval objects
    job_string = replace_eval(job_string, conf_object, int(conf_object.i0))

    file_object = open(path, "w")
    file_object.write(job_string)


# utility functions
def frange(start, end, num):
    """
    Simple range function for floats.
    :param start: float at start of range
    :param end: float at end of range
    :param num: total number of floats in range
    :return: range array with floats
    """
    r = []
    d = (end - start) / (num - 1)
    for k in range(0, num):
        r.append(start + k * d)
    return r


def make_sure_path_exists(path):
    """
    Creates directory if needed.
    :param path: path to directory
    :return:
    """
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def replace_eval(string, conf_object, i):
    """
    Replaces %eval ...% expressions in string by the evaluated version.
    :param string: string to replace objects
    :param conf_object:
    :param i: value of index i
    :return: string with %eval ...$ expressions evaluated
    """
    # execute expression string
    myglobals = {}
    mylocals = {}
    mylocals['i'] = i
    mylocals['i0'] = conf_object.i0
    mylocals['i1'] = conf_object.i1
    exec(conf_object.exec_string, myglobals, mylocals)

    # find, evaluate and replace all eval objects
    for eval_line in re.finditer("%eval\s([^%]*)%", string):
        current_string = eval_line.group()
        eval_command = eval_line.group(1)
        result = eval(eval_command, myglobals, mylocals)
        string = string.replace(current_string, str(result), 1)

    return string


class Object(object):
    pass


if __name__ == "__main__":
    main()
