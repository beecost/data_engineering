CLASSPATH=

CLASSPATH=${PWD}/conf:${CLASSPATH}

if [ -d ${PWD}/build/classes ]; then
	CLASSPATH=${CLASSPATH}:${PWD}/build/classes;
fi

if [ -d ${PWD}/build ]; then
    for f in ${PWD}/build/*.jar; do
        CLASSPATH=${CLASSPATH}:$f;
    done
fi

if [ -d ${PWD}/lib ]; then
    for f in ${PWD}/lib/*.jar; do
        CLASSPATH=${CLASSPATH}:$f;
    done
fi

if [ "$JAVA_HOME" = "" ]; then
    JAVA_HOME=`readlink -f \`which java\` | sed "s/^\(.*\)\/bin\/java/\\1/"`
    JAVA=$JAVA_HOME/bin/java
    if [ ! -x $JAVA_HOME/bin/java ]; then
        echo "Error: No suitable jvm found. Using default one." > /dev/stderr
        JAVA_HOME=""
        JAVA=java
    fi
else
    JAVA=$JAVA_HOME/bin/java
fi

