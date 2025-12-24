#!/bin/bash

case "$1" in
start)
  # Example: Start docker containers, set environment variables, etc.
  docker start postgis
  ;;
exit)
  # Example: Stop docker containers, cleanup, etc.
  docker stop postgis
  ;;
*)
  echo "Usage: $0 {start|exit}"
  exit 1
  ;;
esac
