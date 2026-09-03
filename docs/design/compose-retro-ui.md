# Rendu "jeu rétro" en Jetpack Compose

Pistes techniques pour donner à une app (pas un jeu) une esthétique jeu vidéo rétro : terminal 8-bit, palette limitée, pixel art, CRT.

---

## 1. Pixel art net (pas de flou)

Compose lisse les images par défaut. Pour du pixel art net, forcer le nearest-neighbor :

```kotlin
Image(
    bitmap = spriteBitmap,
    contentDescription = null,
    filterQuality = FilterQuality.None, // clé pour du pixel art net
    modifier = Modifier.size(128.dp)
)
```

## 2. Police pixel

Utiliser une police bitmap via Google Fonts :
- **Press Start 2P** : très marquée 8-bit, réservée aux titres / courts labels (illisible en paragraphe)
- **VT323** : plus lisible, bon compromis pour le corps de texte / style terminal

```kotlin
val pressStart2P = GoogleFont("Press Start 2P")
val fontFamily = FontFamily(
    Font(googleFont = pressStart2P, fontProvider = provider)
)

Text("GAME OVER", fontFamily = fontFamily, fontSize = 24.sp)
```

## 3. Palette limitée

C'est l'élément le plus important pour l'effet rétro, indépendamment de la technique de rendu.
- Game Boy : 4 teintes de vert
- NES : palette 8-16 couleurs
- Gruvbox / terminal : tons chauds désaturés, fond sombre

Définir la palette dans un thème Compose custom plutôt que MaterialTheme classique (voir section 7).

## 4. Boutons et bordures "pixel" (le composant clé)

Dessiner des bordures en segments rectangulaires façon NES (relief en escalier, pas de coins arrondis, pas d'ombre floue). Technique : `pixelSize` fixe (ex. 4.dp), dessiner les segments horizontaux/verticaux via `Canvas` ou `drawBehind`, inverser ombre/highlight sur l'état pressed.

Référence détaillée : *"Pixel Perfect: Creating a Retro Button with Jetpack Compose"*
`https://medium.com/@kiwi47/pixel-perfect-creating-a-retro-button-with-jetpack-compose-4b2697291fbb`
Code complet : `https://gist.github.com/kiwi4747/4fe0590baa046ed3eff7e52e270f3824`

Ce composant `PixelContainer`/`PixelButton` est réutilisable pour boutons, cartes, champs de texte, dialogues — c'est la brique de base d'un vrai design system rétro.

## 5. Effet CRT / scanlines (AGSL, Android 13+)

Compose expose `RuntimeShader` pour écrire un shader AGSL (scanlines, aberration chromatique, vignettage, distorsion de lentille). À utiliser avec parcimonie sur du texte long (fatigue visuelle).

```kotlin
val shader = remember {
    RuntimeShader("""
        uniform shader composable;
        uniform float2 resolution;
        half4 main(float2 fragCoord) {
            half4 color = composable.eval(fragCoord);
            float scanline = sin(fragCoord.y * 3.14159 * 0.8) * 0.08;
            color.rgb -= scanline;
            return color;
        }
    """.trimIndent())
}

Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        renderEffect = android.graphics.RenderEffect
            .createRuntimeShaderEffect(shader, "composable")
            .asComposeRenderEffect()
    }
) {
    // contenu de l'app
}
```

Pour les versions plus anciennes ou en Compose Multiplatform : simuler les scanlines "à la main" avec un `Canvas` qui dessine des lignes horizontales semi-transparentes par-dessus le rendu.

Projets de référence :
- **Cathode-AGSL** — simulation CRT complète (distorsion, jitter, aberration chromatique) : `https://github.com/JumpingKeyCaps/Cathode-AGSL`
- **shady** — galerie de shaders AGSL en Compose, bonne base pédagogique : `https://github.com/drinkthestars/shady`
- **vortex** — playground AGSL/RenderEffect en Compose : `https://github.com/vishal2376/vortex`

## 6. Animation "low frame rate" volontaire

Éviter les interpolations trop fluides. Faire des updates par "tick" discret plutôt que des animations continues, et privilégier des transitions "cut" (snap) plutôt que des fondus entre écrans :

```kotlin
LaunchedEffect(Unit) {
    while (true) {
        delay(100) // ~10 fps, façon jeu 8-bit
        frameIndex = (frameIndex + 1) % spriteFrames.size
    }
}
```

## 7. Structurer un vrai thème custom (pas MaterialTheme)

Pour que l'esthétique soit cohérente sur toute l'app plutôt que bricolée composant par composant, définir un design system custom exposé via `CompositionLocalProvider` (colors, typography, shapes, dimensions propres, pas de convention Material).

Référence : *"How to build a custom design system with Jetpack Compose"*
`https://proandroiddev.com/how-to-build-a-custom-design-system-with-jetpack-compose-980ecf2fc78d`

## 8. Dessiner directement en pixels avec Canvas

Pour des sprites/icônes définis par grille de pixels (icônes custom, illustrations) :

```kotlin
Canvas(modifier = Modifier.size(256.dp)) {
    val pixelSize = size.width / 16f
    spriteGrid.forEachIndexed { y, row ->
        row.forEachIndexed { x, colorIndex ->
            if (colorIndex != 0) {
                drawRect(
                    color = palette[colorIndex],
                    topLeft = Offset(x * pixelSize, y * pixelSize),
                    size = Size(pixelSize, pixelSize)
                )
            }
        }
    }
}
```

---

## Inspiration visuelle (non-Compose, mais utile comme catalogue)

**8bitcn-ui** — bibliothèque de composants React/shadcn en thème 8-bit complet (login forms, dashboards, cartes, terminal shell). Pas transposable en code, mais excellent catalogue de patterns UI à recréer en Compose.
`https://github.com/theorcdev/8bitcn-ui` · démo : `https://www.8bitcn.com`

**Karui ToDo** — todo-list au look terminal rétro (thèmes Gruvbox, commandes façon Unix, easter eggs mini-jeux). Techniquement en Alpine.js/Svelte + Tauri, pas Compose — mais bonne référence d'esthétique terminal/commande à reproduire.
`https://github.com/ronynn/karui`

---

## Résumé des priorités si on part de zéro

1. Choisir une palette limitée (3-8 couleurs) → impact visuel le plus fort pour le moins d'effort
2. Police pixel/mono (VT323 pour le texte courant, Press Start 2P pour les titres courts)
3. Composant `PixelButton`/`PixelContainer` réutilisable (bordures en relief, pas de coins arrondis)
4. Thème custom (`CompositionLocalProvider`) pour propager la cohérence partout
5. Scanlines/CRT en overlay léger — optionnel, à utiliser avec parcimonie
6. Transitions "cut" plutôt que fondues, animations à froid (tick-based) pour renforcer l'ambiance
