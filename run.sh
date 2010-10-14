#!/bin/bash

SCALA=scala
SBT=sbt

MEANDRE2_HOME=`dirname $0`
MEANDRE2_LOG=$MEANDRE2_HOME/log/meandre.log
MEANDRE2_STARTUP_SCRIPT=$MEANDRE2_HOME/scripts/server.scala

# DO NOT MODIFY BELOW THIS LINE

MEANDRE2_JAR=$MEANDRE2_HOME/infrastructure/target/scala_2.7.7/infrastructure_2.7.7-2.0.jar
MEANDRE2_DEPS_DIR=$MEANDRE2_HOME/infrastructure/lib_managed/scala_2.7.7/compile

function buildClasspath() {
  for dep in $MEANDRE2_DEPS_DIR/*.jar
    do
      MEANDRE2_CLASSPATH=$MEANDRE2_CLASSPATH:$dep
    done

  MEANDRE2_CLASSPATH=$MEANDRE2_CLASSPATH:$MEANDRE2_JAR
  MEANDRE2_CLASSPATH=`echo $MEANDRE2_CLASSPATH | cut -c2-`
}


LOG_FOLDER=`dirname $MEANDRE2_LOG`

if [ ! -d $LOG_FOLDER ]; then
  echo "Creating log folder $LOG_FOLDER"
  mkdir -p $LOG_FOLDER
  if [ ! $? -eq 0 ]; then
    echo "Cannot create log folder - check whether you have proper permissions."
    exit 3
  fi
fi

if [ ! -d $MEANDRE2_DEPS_DIR ]; then
  printf "Dependency files are missing... running 'sbt update' to retrieve them..."
  $SBT update 1>>$MEANDRE2_LOG 2>&1 
  if [ $? -eq 0 ]; then
    echo "done"
  else
    echo "failed"
    echo "There was a problem running 'sbt update'. Please check $MEANDRE2_LOG for errors."
    exit 1
  fi
fi

if [ ! -f $MEANDRE2_JAR ]; then
  printf "The Meandre 2.0 package is missing... running 'sbt package' to build it..."
  $SBT package 1>>$MEANDRE2_LOG 2>&1
  if [ $? -eq 0 ]; then
    echo "done"
  else
    echo "failed"
    echo "There was a problem running 'sbt package'. Please check $MEANDRE2_LOG for errors."
    exit 2
  fi
fi

buildClasspath

$SCALA -classpath $MEANDRE2_CLASSPATH $MEANDRE2_STARTUP_SCRIPT 1>>$MEANDRE2_LOG 2>&1 &

echo The Meandre 2.0 server is now starting... you can access it at http://$(hostname):1714
