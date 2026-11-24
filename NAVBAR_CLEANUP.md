# Glass navigation bar cleanup

- Added explicit clipping around the backdrop/lens drawing so the visual effect is constrained to the rounded navigation pill.
- Kept the existing drag-after-long-press behavior, tab selection callback, and spring indicator animation unchanged.
- Kept the outer layout transparent; no page-wide or bottom-bar background was added.

If a rectangular area is still visible after this change, it may be coming from the device's system navigation-bar contrast scrim or edge-to-edge window configuration rather than `GlassNavBar` itself.
