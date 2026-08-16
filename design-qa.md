# Splash screen design QA

final result: passed

## Evidence

- Day reference: `/Users/byron/.codex/generated_images/019fe6e5-5034-7531-87ca-f77ff9731882/exec-f55d8b58-935d-417f-83e7-0f24bf430d94.png` (853 x 1844).
- Night reference: `/Users/byron/.codex/generated_images/019fe6e5-5034-7531-87ca-f77ff9731882/exec-2b7cb82b-8065-4b71-9509-358a1aa167d0.png` (853 x 1844).
- Day implementation frame: `/tmp/minimalistweather-splash-qa/day-implementation-final.png` (672 x 1496).
- Night implementation frame: `/tmp/minimalistweather-splash-qa/night-implementation-final.png` (672 x 1496).
- Day comparison: `docs/design-qa/splash-day-comparison.png`.
- Night comparison: `docs/design-qa/splash-night-comparison.png`.
- Cold-start recordings: `/tmp/minimalistweather-splash-qa/weather-splash-day-final.mp4` and `/tmp/minimalistweather-splash-qa/weather-splash-night-final.mp4`.

## Verification environment

- Device: Pixel 10 Pro XL Android Emulator.
- Android: 17 / API 37.
- Physical viewport: 1344 x 2992 at 480 dpi.
- Comparison viewport: 672 x 1496, preserving the device aspect ratio at half resolution.
- State: force-stopped cold launch in explicit light mode and explicit dark mode.
- Reference normalization: center-cropped to the implementation aspect ratio before side-by-side comparison.

## Findings

- No P0, P1, or P2 visual issues remain.
- Day and night resources switch correctly from system light/dark mode before the first app frame.
- Both marks remain centered, uncropped, and legible inside the Android system splash icon safe area.
- The selected blue and navy backgrounds match the sampled reference colors.
- Cold-start sequences transition directly to weather content without the previous centered circular loading indicator.
- Android status/navigation bars and the solid-color splash background are intentional platform constraints of the system SplashScreen API.

## Comparison history

1. Initial implementation: composition and colors matched, but the system-rendered weather marks appeared smaller than the selected references.
2. Final implementation: enlarged both transparent assets by 22%, rebuilt, reinstalled, and recaptured light/dark cold starts. The result is balanced without clipping the night stars.

## Fidelity surfaces

- Assets: real transparent raster assets derived from the selected day and night references; no placeholder or emoji substitutions.
- Layout: centered composition and generous negative space preserved in both modes.
- Typography: the concept contains no product typography; only platform status-bar text is present.
- Visual styling: reference palette, soft cloud shading, warm sun/moon tones, and five-star night arrangement preserved.
- Interaction and state: cold launch, automatic day/night resource selection, 280 ms fade/scale exit, and post-splash content transition verified; no loading spinner appears.
