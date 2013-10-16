#!/bin/bash

if [ "$1" = "svcompfolder" ]; then
echo "specified folder in a test setting"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 $2 \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-simpleTest-SVCOMP"
fi

if [ "$1" = "0" ]; then
echo "testing minitests"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/programs/minitests/quantifier \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-simpleTest-SVCOMP"
fi


if [ "$1" = "1" ]; then
echo "testing our example programs with different block encodings"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/programs/minitests \
"TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-EagerPost-Hoare" \
"TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncoding-EagerPost-Hoare" \
"TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncodingNoParallel-EagerPost-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-Lazypost-Hoare" \
"TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncoding-Lazypost-Hoare" \
"TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncodingNoParallel-Lazypost-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare" \
"TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncoding-StrongestPost-Hoare" \
"TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncodingNoParallel-StrongestPost-Hoare"
fi



if [ "$1" = "2" ]; then
echo "small test"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/programs \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-EagerPost-Hoare" \
 "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-BlockEncoding-EagerPost-Hoare"
fi

if [ "$1" = "3" ]; then
echo "testing TraceCheckerSpWp"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/programs \
"TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SpWp" \
"TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare"
fi

if [ "$1" = "4" ]; then
echo "testing TraceCheckerSpWp for SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ssh-simplified/ \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SpWp-SVCOMP" \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SVCOMP" 
trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ntdrivers-simplified \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SpWp-SVCOMP" \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SVCOMP" 
trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/systemc \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SpWp-SVCOMP" \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SVCOMP" 
trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/programs \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SpWp-SVCOMP" \
 "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-LargeStatements-StrongestPost-Hoare-SVCOMP" 
fi


if [ "$1" = "5" ]; then
echo "testing different interpolations"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/programs \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Nested-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Tree-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-ForwardPredicates-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Nested-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Tree-Hoare" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-ForwardPredicates-Hoare"
fi

if [ "$1" = "6" ]; then
echo "testing different interpolations on SV-COMP examples"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/svcomp13/ssh-simplified/ \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-ForwardPredicates-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-ForwardPredicates-SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/svcomp13/ntdrivers-simplified \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-ForwardPredicates-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-ForwardPredicates-SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/svcomp13/systemc \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-ForwardPredicates-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-ForwardPredicates-SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/programs \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeSeq-ForwardPredicates-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Nested-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-Tree-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-MlbeLoop-ForwardPredicates-SVCOMP"
fi


if [ "$1" = "7" ]; then
echo "testing SmtInterpol vs. Z3 on SV-COMP examples"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/c/ssh-simplified/ \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-SmtInterpol-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-Z3-SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/c/ntdrivers-simplified \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-SmtInterpol-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-Z3-SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/c/systemc \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-SmtInterpol-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-Z3-SVCOMP"
trunk/examples/toolchains/TraceAbstractionTestDir.sh 120 trunk/examples/programs \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-SmtInterpol-SVCOMP" \
"TraceAbstraction.xml;TraceAbstractionC.xml;Automizer-UnsatCoreSmtInterpolZ3/Automizer-MlbeLoop-ForwardPredicates-Z3-SVCOMP"
fi




#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/systemc \
#"TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-StrongestMinimize" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-EagerMinimize" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LazyMinimize" \

#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ssh-simplified/ \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" 
#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ntdrivers-simplified \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" 
#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/systemc \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" 
#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/programs \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" 

# trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ssh-simplified/ \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongestMinimizeSevpa"
# trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ntdrivers-simplified \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongestMinimizeSevpa"
# trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/systemc \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongestMinimizeSevpa"
# trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/programs \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongestMinimizeSevpa"


#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ssh-simplified/ \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeLazy" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingLazy" 
#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/ntdrivers-simplified \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeLazy" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingLazy"
#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/svcomp13/systemc \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeLazy" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingLazy" 
#trunk/examples/toolchains/TraceAbstractionTestDir.sh 1000 trunk/examples/programs \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeStrongest" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingStrongest" \
# "TraceAbstraction.xml;TraceAbstractionC.xml;TraceAbstraction-svcomp-LargeLazy" \
# "TraceAbstractionWithBlockEncoding.xml;TraceAbstractionCWithBlockEncoding.xml;TraceAbstraction-svcomp-BlockEncodingLazy" 

# trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/rank \
#  "BuchiAutomizer.xml;BuchiAutomizerC.xml;BuchiAutomizer"
# trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/programs \
#  "BuchiAutomizer.xml;BuchiAutomizerC.xml;BuchiAutomizer"
# trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/terminator \
#  "BuchiAutomizer.xml;BuchiAutomizerC.xml;BuchiAutomizer"
# trunk/examples/toolchains/TraceAbstractionTestDir.sh 20 trunk/examples/svcomp13 \
#  "BuchiAutomizer.xml;BuchiAutomizerC.xml;BuchiAutomizer"
