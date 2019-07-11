#!/bin/bash

# Wrapper for java console command. You can use several environment variables to alter JVM options.
# 
# Heap size is 1 Gb.
#   Set JAVA_MEM to change the default heap size (e.g. "2g", "2048m")
# No GC options are set.
#   Set JAVA_GC_OPTIONS to provide your options, or
#   set JAVA_GC to CMS, SER, PAR or PAROLD to quickly set the desired GC type.
# Java GC logging is enabled, 'logs/<PID>.gc' is the log faile.
#   Set JAVA_DISABLE_GC_LOG to disable it, or
#   set JAVA_GC_LOG_OPTIONS to override default options; or
#   set JAVA_GC_LOG_FILE to change the default log file.
# Yourkit profiler agent is disabled.
#   Set JAVA_ENABLE_YOURKIT to enable it;
#   set JAVA_YOURKIT_AGENT_PATH to change the default path to the agent library.
# Remote debugging is disabled.
#   Set JAVA_ENABLE_REMOTE_DEBUG to enable it (default port is 1044), or
#   set JAVA_REMOTE_DEBUG_OPTIONS to override default options;
#   set JAVA_REMOTE_DEBUG_PORT to change the default port, 
#   set JAVA_REMOTE_DEBUG_SUSPEND to "y" to suspend the program until a debugger is attached.
# Compress object reference size is enable
#	in 64bits OS, remove JAVA_COMPRESS_REF_OPTIONS to use default (64 bits) object reference size
# Log4j configuration file is 'log4j.properties' placed in CLASSPATH.
#   Set LOG4J_CONFIG to change the default path.
# Additional JVM options can be set in JAVA_OPTIONS variable.

if [ $# = 0 ]; then
	echo "Usage: $0 <class> <arguments>" >&2
	exit 1
fi
CLASS=$1
shift

# we start with user provided JAVA_OPTIONS. it contains additional JVM options

# heap size
if [ -z $JAVA_MEM ]; then
	JAVA_MEM="1g"
fi
JAVA_OPTIONS="$JAVA_OPTIONS -Xms$JAVA_MEM -Xmx$JAVA_MEM -server -Dfile.encoding=UTF8"

# compressed size of an object reference, use this when heap size <= 32GB
#JAVA_COMPRESS_REF_OPTIONS="-XX:+UseCompressedOops"
if [ ! -z  "$JAVA_COMPRESS_REF_OPTIONS" ]; then
    JAVA_OPTIONS="$JAVA_OPTIONS $JAVA_COMPRESS_REF_OPTIONS"
	echo "using compressed object references:$JAVA_COMPRESS_REF_OPTIONS"
fi

JAVA_GC_OPTIONS="-XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseParNewGC -XX:NewSize=64m -XX:MaxNewSize=64m -XX:SurvivorRatio=8 -XX:+PrintTenuringDistribution -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:+UseTLAB -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:ParallelGCThreads=4 -XX:CMSInitiatingOccupancyFraction=95 -XX:+UseCMSInitiatingOccupancyOnly -XX:GCTimeRatio=19 -XX:MaxGCPauseMillis=1000 -XX:+NeverTenure"

# GC options
if [ -z "$JAVA_GC_OPTIONS" ]; then
	if [ "$JAVA_GC" == "CMS" ]; then
		JAVA_GC_OPTIONS="-XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseParNewGC"
	elif [ "$JAVA_GC" == "SER" ]; then
		JAVA_GC_OPTIONS="-XX:+UseSerialGC"
	elif [ "$JAVA_GC" == "PAR" ]; then
		JAVA_GC_OPTIONS="-XX:+UseParallelGC"
	elif [ "$JAVA_GC" == "PAROLD" ]; then
		JAVA_GC_OPTIONS="-XX:+UseParallelOldGC"
	elif [ -z "$JAVA_GC" ]; then
		JAVA_GC_OPTIONS=
	else
		echo "Unknown JAVA_GC option : $JAVA_GC" >&2
		exit 2
	fi
fi
JAVA_OPTIONS="$JAVA_OPTIONS $JAVA_GC_OPTIONS"

# GC logging
# other useful options: -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution
if [ -z "$JAVA_DISABLE_GC_LOG" ]; then
	if [ -z "$JAVA_GC_LOG_OPTIONS" ]; then
		if [ -z "$JAVA_GC_LOG_FILE" ]; then 
			JAVA_GC_LOG_FILE="logs/$$.gc"
		fi
		JAVA_GC_LOG_OPTIONS="-verbose:gc -Xloggc:$JAVA_GC_LOG_FILE -XX:+PrintGCDetails"
	fi
	JAVA_OPTIONS="$JAVA_OPTIONS $JAVA_GC_LOG_OPTIONS"
fi

# remote debugging
if [ ! -z $JAVA_ENABLE_REMOTE_DEBUG ] || [ ! -z $JAVA_REMOTE_DEBUG_OPTIONS ]; then
	if [ -z $JAVA_REMOTE_DEBUG_OPTIONS ]; then
		if [ -z $JAVA_REMOTE_DEBUG_PORT ]; then
			JAVA_REMOTE_DEBUG_PORT="1044"
		fi
		if [ -z $JAVA_REMOTE_DEBUG_SUSPEND ]; then
			JAVA_REMOTE_DEBUG_SUSPEND="n"
		fi
		JAVA_REMOTE_DEBUG_OPTIONS="transport=dt_socket,server=y,suspend=$JAVA_REMOTE_DEBUG_SUSPEND,address=$JAVA_REMOTE_DEBUG_PORT"
	fi
	JAVA_OPTIONS="$JAVA_OPTIONS -Xdebug -Xrunjdwp:$JAVA_REMOTE_DEBUG_OPTIONS"
fi

# log4j coinfiguration
if [ ! -z "$LOG4J_CONFIG" ]; then
	JAVA_OPTIONS="$JAVA_OPTIONS -Dlog4j.configuration=$LOG4J_CONFIG"
fi

# sets CLASSPATH, JAVA_HOME, JAVA; updates JAVA_OPTIONS
. bin/setjavavars.sh
export CLASSPATH
export JAVA_HOME
# gogogo
exec $JAVA $JAVA_OPTIONS $CLASS "$@"
