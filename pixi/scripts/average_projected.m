(*
Mathematica script for averaging binary output files of ProjectedEnergyDensity2.
Openpixi output files need to be inside some folder and named pe1.dat, pe2.dat, pe3.dat, ...
The output of this script will be a plaintext Mathematica List.

	Usage:
		module load Mathematica/X.X.X
		math -script average_projected.m <input path> <output path> <number of events>

	Example:
		In './output/t01_bi2/' there are 20 files called pe1.dat, ... , pe20.dat.
		Averaged output will be written to ./math_output/t01_bi2.dat.

		math -script average_projected.m ./output/t01_bi2/ ./math_output/ 20

		Import data into Mathematica using:

		data = Get["./math_output/t01_bi2.dat"]

*)

args = $CommandLine;
inputpath = args[[4]];
outputpath = args[[5]];
num = ToExpression[args[[6]]];
path = FileNameJoin[FileNameSplit[inputpath]] <> "/";
datafile = FileNameJoin[FileNameSplit[outputpath]] <> "/" <> FileNameSplit[inputpath][[-1]] <> ".dat"

readNewBinary[file_] := 
Module[{t, NL, timesteps, time, ET, BT, EL, BL, SL, JE, actualbytecount, expectedbytecount, stream},

	stream = OpenRead[file, BinaryFormat -> True];
	SetStreamPosition[stream, 0];

	{NL, timesteps} = BinaryRead[file, {"Integer32", "Integer32"}, ByteOrdering -> +1];

	actualbytecount = FileByteCount[file];
	expectedbytecount = 4 + 4 + (8 + NL * 8 * 6) * timesteps;

	ET = {};
	BT = {};
	EL = {};
	BL = {};
	SL = {};
	JE = {};

	For[t = 0, t < timesteps, t++,
		time = BinaryRead[stream, "Real64", ByteOrdering -> +1];
		AppendTo[ET, BinaryReadList[stream, "Real64", NL, ByteOrdering -> +1]];
		AppendTo[BT, BinaryReadList[stream, "Real64", NL, ByteOrdering -> +1]];
		AppendTo[EL, BinaryReadList[stream, "Real64", NL, ByteOrdering -> +1]];
		AppendTo[BL, BinaryReadList[stream, "Real64", NL, ByteOrdering -> +1]];
		AppendTo[SL, BinaryReadList[stream, "Real64", NL, ByteOrdering -> +1]];
		AppendTo[JE, BinaryReadList[stream, "Real64", NL, ByteOrdering -> +1]];
	];
	Close[stream];
	{ET, BT, EL, BL, SL, JE}
]

ETe = {};
BTe = {};
ELe = {};
BLe = {};
Se = {};
JEe = {};
For[i = 1, i <= num, i++,
	filepath = path <> StringReplace["pe<i>.dat", "<i>" -> ToString[i]];
	Print["Loading " <> filepath];
	event = readNewBinary[filepath];
	AppendTo[ETe, event[[1]]];
	AppendTo[BTe, event[[2]]];
	AppendTo[ELe, event[[3]]];
	AppendTo[BLe, event[[4]]];
	AppendTo[Se, event[[5]]];
	AppendTo[JEe, event[[6]]];
];

Print["Averaging event(s) .."];

ET = Mean[ETe];
BT = Mean[BTe];
EL = Mean[ELe];
BL = Mean[BLe];
S = Mean[Se];
JE = Mean[JEe];

data = {ET, BT, EL, BL, S, JE};

Put[data, datafile];

Print["Wrote averaged observables to " <> datafile];



