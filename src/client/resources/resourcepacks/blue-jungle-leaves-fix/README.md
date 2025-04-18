# Blue Jungle Leaves Fix for Visual Snowy Leaves

Fixes jungle leaves appearing blue by separating the "fruits" from the base
leaves. Runtime modification of loaded textures affected the vanilla jungle
leaves texture such that the leaves appear blue when snowy. This in due to the
algorithm confidently selecting the brightest color, which is conveniently the
"fruit" pixels. Some calculation later causes the leaves portion to appear blue.
Separating the textures allows setting different tint index for the block's
model; the basis for handling texture snowification. This still allow the
"fruit" portion to be snowified as well.
