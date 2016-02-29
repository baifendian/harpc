#!/bin/sh
#remove old logs
cd ..
rm -rf log*
echo "remove old logs result is $?"

#restart service
cd etc
uwsgi --reload harpc_admin.pid
echo "restart service result is $?"
