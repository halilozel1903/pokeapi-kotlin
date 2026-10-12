#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# The sample opens each screen from the `scene` extra and loads it live from PokeAPI, with the official artwork.
# Each scene logs when its data and every image on screen have loaded; a scene that shows an error or does not
# finish loading fails the run. Every capture is also checked for the expected text and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: grid and Bulbasaur side by side, light and dark
#   bash scripts/screenshots.sh phone    # pixel_7: grid, Pikachu's stats, Eevee's evolutions, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it. None of these appear on the error screen.
expected_text() {
  case "$1" in
    grid) echo "Bulbasaur" ;;
    detail) echo "Pikachu" ;;
    evolution) echo "ways to evolve" ;;
    tablet) echo "Profile" ;;
  esac
}

suffix() {
  if [ "$1" = dark ]; then echo "-dark"; else echo ""; fi
}

install_sample
if [ "$device" = tablet ]; then
  ensure_landscape
  for mode in light dark; do
    set_night_mode "$mode"
    fresh_launch --es scene tablet
    wait_for_scene tablet
    capture "tablet-pokedex$(suffix "$mode")" "$(expected_text tablet)"
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in grid detail evolution; do
      fresh_launch --es scene "$scene"
      wait_for_scene "$scene"
      capture "phone-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
fi
