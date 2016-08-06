#!/bin/sh

cd $(dirname $0)
base_url=$(../base_url.sh)

curl \
  --request GET --include \
  -H "Content-Type: application/json" \
  $base_url/player/states