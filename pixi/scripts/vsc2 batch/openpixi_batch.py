"""
        OPENPIXI SGE BATCH SCRIPT

    This script creates a batch of yaml files and job files based on the template files and submits them using qsub.

    Example usage:
    Parse the input file 'pancakewidths', create yaml and qjob files in 'pancake_widths/' and submit them.
        python openpixi_batch.py -cs -i pancakewidths -o pancakewidths_files/

    Parse the input file 'coupling' and create yaml and qjob files in 'pancake_widths/'.
        python openpixi_batch.py -cs -i coupling -o coupling_files/

    Submit all *.qjob files in the 'jobs' folder.
        python openpixi_batch.py -s -o jobs/

    Options:
    -c or --create: create YAML and qjob files based on the input file path and write them to output path.
    -s or --submit: submit qjob files in the output path.
    -i INPUT or --input=INPUT: read yaml and qjob templates from INPUT
    -o OUTPUT or --output=OUTPUT: path where yaml and qjob are files are stored

    NOTE: when script is called to create yaml and qjob files, all of the files in the output path are deleted.

    Some options for YAML/QJOB templates:

    1) Defining the integer range
    The first line must define the integer range for the simulations.
    %range BEGIN END%

    Example:
    %range 0 9% for 10 simulations with integers 0, 1, .., 9.

    2) Define path to jar file
    After the first line include the path to the jar-file.

    Example:
    %jar pixi-0.6-SNAPSHOT.jar% uses the jar-file pixi-0.6-SNAPSHOT.jar in the same directory as the input file.

    3) Using integer from range
    Use %i% to insert the integer from the given range.

    Example:
    Use "output%i%.dat" to get output files "output0.dat", "output1.dat", ..., "output9.dat" given the range above.

    4) Float intervals
    Use %f BEGIN END% to define a linear range of floating point numbers.

    Example:
    %f 0.0 0.9% to get the values 0.0, 0.1, ... 0.9 using the integer range above.

"""

import re
import os
import errno
from optparse import OptionParser


def main():
    """Main function of the script. Checks for given arguments and does whatever is to be done."""

    parser = OptionParser()
    parser.add_option("-c", "--create", action="store_true", dest="create", default=False,
                      help="create YAML and qjob files based on input file.")

    parser.add_option("-s", "--submit", action="store_true", dest="submit", default=False,
                      help="submit all *.qjob files in output path.")

    parser.add_option("-i", "--input", action="store", type="string", dest="input", metavar="INPUT",
                      help="path to input file (yaml and qjob template)")

    parser.add_option("-o", "--output", action="store", type="string", dest="output", metavar="OUTPUT",
                      help="output path (where yaml and qjob files will be stored)")

    (options, args) = parser.parse_args()

    input_path = options.input
    output_path = options.output

    if options.create:
        if input_path is not None:
            if output_path is not None:
                create_files(input_path, output_path)
            else:
                print("Error: Output path not defined.")
                exit(-1)
        else:
            print("Error: Input path not defined.")
            exit(-1)

    if options.submit:
        if output_path is not None:
            submit_qjobs(output_path)
        else:
            print("Error: Path to job files not defined.")
            exit(-1)


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


def submit_qjobs(o_path):
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


def create_files(i_path, o_path):
    """
    Creates yaml and qjob files based on given script.
    :param i_path: path to input script
    :param o_path: path for output files
    :return:
    """
    script_string = open(i_path, "r").read()

    # find and read integer range line (should be first line, but doesnt really matter.)
    (b, e) = re.search("%range.*%", script_string).span()
    (range_begin, range_end) = re.sub("%", "", re.sub("%range\s", "", script_string[b:e])).split(" ")
    int_range = range(int(range_begin), int(range_end) + 1)
    int_range_len = len(int_range)
    script_string = script_string.replace(script_string[b:e], "")

    # find and read jar path
    (b, e) = re.search("%jar\s.*%", script_string).span()
    jar_path = re.sub("%", "", re.sub("%jar\s", "", script_string[b:e]))

    # parse yaml and qjob template
    r1 = re.compile("%yaml begin%([\S\s]*)%yaml end%")
    r2 = re.compile("%qjob begin%([\S\s]*)%qjob end%")
    if not r1.search(script_string):
        print("Found no proper YAML template in " + i_path)
        exit(-1)
    if not r2.search(script_string):
        print("Found no proper QJOB template in " + i_path)
        exit(-1)

    yaml_template_string = r1.search(script_string).group(1)
    qjob_template_string = r2.search(script_string).group(1)

    # find float ranges
    i = 0
    float_ranges_limits = []
    for float_line in re.finditer("%f\s.*%", yaml_template_string):
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

    # generate yaml files in output folder
    empty_path(o_path)
    make_sure_path_exists(o_path)
    c = 0
    yaml_files = []
    for i in int_range:
        file_name = "tmp" + str(i) + ".yaml"
        path = os.path.join(o_path, file_name)
        file_object = open(path, "w")
        yaml_files.append(file_name)
        current_yaml_string = str(yaml_template_string)

        # replace all integers
        current_yaml_string = current_yaml_string.replace("%i%", str(i))

        # replace all float float ranges
        float_index = 0
        for fr in float_ranges:
            value = fr[c]
            current_yaml_string = current_yaml_string.replace("%f" + str(float_index) + "%", str(value))
            float_index += 1
        c += 1

        # write finished yaml string to file.
        file_object.write(current_yaml_string)
        file_object.close()

    # create SGE job files for each yaml file based on template
    qjob_files = []
    c = 0
    for yf in yaml_files:
        qjob_file_name = yf.replace(".yaml", ".qjob")
        job_id = yf.replace(".yaml", "")
        job_name = "openpixi_b_" + job_id
        qjob_files.append(qjob_file_name)
        path = os.path.join(o_path, qjob_file_name)
        file_object = open(path, "w")
        current_qjob_string = str(qjob_template_string)
        current_qjob_string = current_qjob_string.replace("%job_name%", job_name)
        current_qjob_string = current_qjob_string.replace("%jar_path%", jar_path)
        path = os.path.join(o_path, yf)
        current_qjob_string = current_qjob_string.replace("%input_path%", path)
        file_object.write(current_qjob_string)
        c += 1


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
    d = (end - start) / num
    for k in range(0, num + 1):
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


if __name__ == "__main__":
    main()
