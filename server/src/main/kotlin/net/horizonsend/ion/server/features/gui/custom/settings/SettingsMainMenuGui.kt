package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import kotlin.math.ceil
import kotlin.math.min

object SettingsMainMenuGui : AbstractBackgroundPagedGui {
    private const val SETTINGS_PER_PAGE = 5
    private const val PAGE_NUMBER_VERTICAL_SHIFT = 4

    private val BUTTONS_LIST = listOf(
        SidebarSettingsButton(),
        HudSettingsButton(),
        OtherSettingsButton()
    )

    override fun createGui(): PagedGui<Item> {
        val gui = PagedGui.items()

        gui.setStructure(
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "< . . . . . . . >"
        )

        gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', GuiItems.LeftItem())
            .addIngredient('>', GuiItems.RightItem())

        for (button in BUTTONS_LIST) {
            gui.addContent(button)

            for (i in 1..8) {
                gui.addContent(GuiItems.BlankItem(button))
            }
        }

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {

        // create a new GuiText builder
        val header = "Settings"
        val guiText = GuiText(header)
        guiText.addBackground()

        // get the index of the first setting to display for this page
        val startIndex = currentPage * SETTINGS_PER_PAGE

        for (buttonIndex in startIndex until min(startIndex + SETTINGS_PER_PAGE, BUTTONS_LIST.size)) {

            val title = BUTTONS_LIST[buttonIndex].text
            val line = (buttonIndex - startIndex) * 2

            // setting title
            guiText.add(
                component = title,
                line = line,
                horizontalShift = 21,
                verticalShift = 5
            )
        }

        // page number
        val pageNumberString =
            "${currentPage + 1} / ${ceil((BUTTONS_LIST.size.toDouble() / SETTINGS_PER_PAGE)).toInt()}"
        guiText.add(
            text(pageNumberString),
            line = 10,
            GuiText.TextAlignment.CENTER,
            verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
        )

        return guiText.build()
    }

    private class SidebarSettingsButton : GuiItems.AbstractButtonItem(
        text("Sidebar Settings").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarGui.open(player)
        }
    }

    private class HudSettingsButton : GuiItems.AbstractButtonItem(
        text("HUD Settings").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsHudGui.open(player)
        }
    }

    private class OtherSettingsButton : GuiItems.AbstractButtonItem(
        text("Other Settings").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsOtherGui.open(player)
        }
    }

    class ReturnToMainMenuButton : GuiItems.AbstractButtonItem(
        text("Return to Main Menu Settings").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
            it.setCustomModelData(GuiItem.DOWN.customModelData)
            it.displayName(text("Return to Main Menu Settings").decoration(ITALIC, false))
        }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsMainMenuGui.open(player)
        }
    }
}