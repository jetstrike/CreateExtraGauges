# Extra Gauges [Forked]

This is a custom fork of the **Create: Extra Gauges** mod. 

### Why this fork?
This version was specifically created to:
1. **Add Support for the Create: Simulated Navigation Table**: Corrects compatibility so remote **Display Collectors** can seamlessly receive high-frequency tick updates from the Navigation Table (even when it is on moving ships/contraptions).
2. **Correct the Expression Gauge Text Length Bug**: Resolves the UI truncation issue where the Expression Gauge's settings screen restricted saved expressions/texts to the default 32-character limit instead of the intended 128 characters.
3. **Fix Server Crashes**: Corrects a class loading mixin issue (`ClassNotFoundException: net.minecraft.client.Minecraft`) that crashed dedicated server environments.
4. **Fix Chunk Loading Race Conditions**: Resolves a bug where linked Display Collectors in the same chunk were lost on world reload due to premature NBT validation.

### Features
All original features of Create: Extra Gauges are preserved, including the various analog, digital, and custom expression-based panels/gauges to connect your logic networks together!
