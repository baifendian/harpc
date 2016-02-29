#!/bin/sh
#remove old logs
cd ..
rm -rf log*
echo "remove old logs result is $?"

#start service
cd etc
uwsgi --ini harpc_admin.ini
echo "start uwsgi service result is $?"
