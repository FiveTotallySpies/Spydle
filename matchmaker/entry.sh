#!/bin/bash
exec java -jar -Dspring.profiles.active="$ENV" /app/matchmaker.jar &
pid=$!
# Trap the SIGTERM signal and forward it to the main process (15 = SIGTERM)
trap 'kill -15 $pid; wait $pid' SIGTERM
wait $pid