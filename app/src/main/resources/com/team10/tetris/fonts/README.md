# Bundled font

- Family: **Noto Sans KR** (Korean regional subset)
- Weights: Regular and Bold, static OTF files; unmodified upstream binaries
- Upstream release: `Sans2.004`, https://github.com/notofonts/noto-cjk/tree/Sans2.004/Sans/SubsetOTF/KR
- Font license: SIL Open Font License 1.1; see `OFL.txt` and the copyright metadata embedded in each OTF.
- Regular SHA-256: `69975a0ac8472717870aefeab0a4d52739308d90856b9955313b2ad5e0148d68`
- Bold SHA-256: `5a6ceb287ed2fc6cfc6213144ebea68cbd94b20fc9eb873d8486493bf02d9bda`

`AppFonts.apply()` registers both resources and attaches `fonts.css` before a screen is styled. UI CSS uses the single family `Noto Sans KR`; Canvas text uses `AppFonts.bold()`. No font installation or runtime network connection is required. This standardizes font resources across platforms, though OS rasterization and monitor color reproduction may still differ slightly.
