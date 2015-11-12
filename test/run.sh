#!/bin/bash

  ffmpeg -i $1 -ac 2 -t 120 -filter_complex:a '[0:a]aresample=8000,asplit[l][r]' \
    -map '[l]' -c:a pcm_s16le -f data ./11.bin \
    -map '[r]' -c:a pcm_s16le -f data ./22.bin
