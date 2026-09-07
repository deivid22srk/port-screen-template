# Regras ProGuard/R8.
# O template vai com minify desligado no release. Se o seu port ativar R8,
# mantenha as regras abaixo para preservar o contrato do ViewModel.
-keepclassmembers class com.porttemplate.screen.viewmodel.** { <init>(...); }
