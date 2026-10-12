package io.github.halilozel1903.pokeapi.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.types.PokeType

/**
 * A pill with the type's icon and name in the type color. With [onClick] it works as a filter chip: unselected
 * chips are outlined, selected ones filled.
 *
 * @param compact a smaller chip for cards and lists.
 * @param label the text; the English type name by default.
 */
@Composable
public fun TypeChip(
    type: PokeType,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null,
    compact: Boolean = false,
    showIcon: Boolean = true,
    label: String = type.displayName,
) {
    val background by animateColorAsState(if (selected) type.color else Color.Transparent, label = "chip background")
    val content by animateColorAsState(
        if (selected) type.onColor else MaterialTheme.colorScheme.onSurface,
        label = "chip content",
    )
    val shape = RoundedCornerShape(50)
    val clickable = if (onClick != null) {
        Modifier
            .semantics { this.selected = selected }
            .clickable(role = Role.Checkbox, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier
            .clip(shape)
            .background(background, shape)
            .then(if (selected) Modifier else Modifier.border(1.dp, type.color, shape))
            .then(clickable)
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 3.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
    ) {
        if (showIcon) {
            TypeIcon(type, Modifier.size(if (compact) 12.dp else 16.dp), tint = if (selected) content else type.color)
        }
        Text(
            label,
            color = content,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A round badge with the type's icon, for tight spaces. */
@Composable
public fun TypeBadge(type: PokeType, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier
            .size(28.dp)
            .clip(RoundedCornerShape(50))
            .background(type.color),
        contentAlignment = Alignment.Center,
    ) {
        TypeIcon(type, Modifier.size(16.dp), contentDescription = type.displayName)
    }
}
