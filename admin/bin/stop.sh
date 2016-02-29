#!/bin/sh
cd ../etc
uwsgi --stop harpc_admin.pid; rm -f harpc_admin.pid;
echo "stop uwsgi service result is $?"
