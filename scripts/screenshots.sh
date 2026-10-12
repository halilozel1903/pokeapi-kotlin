#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# The sample opens each screen from the `scene` extra with the bundled JSON fixtures and drawn creature artwork,
# so no capture depends on the network and every run looks the same.
# Every capture is checked for the expected text and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: grid and Bulbasaur side by side, light and dark
#   bash scripts/screenshots.sh phone    # pixel_7: grid, Charizard's stats, Eevee's evolutions, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it.
expected_text() {
  case "$1" in
    grid) echo "Bulbasaur" ;;
    detail) echo "Base stats" ;;
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
    capture "tablet-pokedex$(suffix "$mode")" "$(expected_text tablet)"
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in grid detail evolution; do
      fresh_launch --es scene "$scene"
      capture "phone-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
fi
