# vcdroid
VCDroid is ViewController library for Android which fixes Activities and Fragments by eliminating them from your app.

## Why should I bother?
Let's face it — Activities and Fragments are trash. And if you're coding for Android you always knew it — maybe just couldn't admit it.

Activities are trash because they enforce your app to be the schizophrenic: fragmented, separated and scattered. Their launch takes ages, their animation is broken, transferring arguments between them inconvinient (try to send bitmap from one to another without making copy of it).

Fragments are trash due to their overly-complex, over-engineered lifecycle, two "standard" implementations — from SDK and from supportlib (one of which is always buggy), child nesting problems and stupid API (FragmentTransaction? FragmentManager? Are you INSANE?).

Other agruments against them: https://corner.squareup.com/2014/10/advocating-against-android-fragments.html

To fix them we need a radical approach, a surgery which will eliminate all the broken, unnecessary parts: we must eliminate the Activity and Fragment usage from our apps. We must implement our own UI component system for Android, which operates on single activity only, supplies backwards-compatible simple transition animation api and works with Views only.

## Modal dialogs
You can easily show complex modal dialogs and dismiss them with returning results from any of child controllers in them.

Just use `presentController()` and `onPresentedDismissed()` methods in presenting controller and `dismissController()` in presented controller.
