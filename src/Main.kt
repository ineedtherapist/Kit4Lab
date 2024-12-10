data class Item(
    val id: Int,
    var name: String,
    var quantity: Int,
    var price: Double
)

// Singleton: ShoppingListManager
class ShoppingListManager private constructor() {
    private val shoppingLists = mutableMapOf<String, ShoppingList>()

    companion object {
        val instance: ShoppingListManager by lazy { ShoppingListManager() }
    }

    fun createList(name: String): ShoppingList {
        val list = ShoppingList(name)
        shoppingLists[name] = list
        return list
    }
}

// Adapter: Зовнішній формат товарів
data class ExternalItem(
    val externalId: String,
    val productName: String,
    val count: Int,
    val unitPrice: Double
)

// Adapter для перетворення ExternalItem в Item
class ItemAdapter(private val externalItem: ExternalItem) {
    fun toItem(nextId: Int): Item {
        return Item(
            id = nextId,
            name = externalItem.productName,
            quantity = externalItem.count,
            price = externalItem.unitPrice
        )
    }
}

// Observer: Інтерфейс спостерігача
interface ShoppingListObserver {
    fun onItemAdded(item: Item)
    fun onItemRemoved(item: Item)
}

// ShoppingList з підтримкою спостерігачів
class ShoppingList(private val name: String) {
    private val items = mutableListOf<Item>()
    private val observers = mutableListOf<ShoppingListObserver>()
    private var nextId = 1 // Лічильник для унікальних ID товарів

    fun addObserver(observer: ShoppingListObserver) {
        observers.add(observer)
    }

    fun getNextId(): Int {
        return nextId
    }

    fun addItem(item: Item) {
        items.add(item.copy(id = nextId++))
        notifyItemAdded(item)
    }

    fun removeItem(itemId: Int) {
        val item = items.find { it.id == itemId }
        if (item != null) {
            items.remove(item)
            notifyItemRemoved(item)
        } else {
            println("Товар з ID $itemId не знайдено у списку.")
        }
    }

    private fun notifyItemAdded(item: Item) {
        observers.forEach { it.onItemAdded(item) }
    }

    private fun notifyItemRemoved(item: Item) {
        observers.forEach { it.onItemRemoved(item) }
    }

    fun displayItems() {
        println("\nСписок покупок '$name':")
        if (items.isEmpty()) {
            println("Список порожній.")
        } else {
            items.forEach { item ->
                println("${item.id}. ${item.name} | Кількість: ${item.quantity} "
                        + "| Ціна за одиницю: ${item.price} | Загальна вартість: ${item.quantity * item.price}")
            }
        }
    }

    fun printReceipt() {
        println("\nФінальний чек для списку '$name':")
        if (items.isEmpty()) {
            println("Чек порожній.")
        } else {
            var totalCost = 0.0
            items.forEach { item ->
                val itemTotal = item.quantity * item.price
                totalCost += itemTotal
                println("${item.id}. ${item.name} | Кількість: ${item.quantity} " +
                        "| Ціна за одиницю: ${item.price} | Загальна вартість: $itemTotal")
            }
            println("Загальна сума: $totalCost")
        }
    }
}

// Реалізація спостерігача
class ShoppingListNotifier : ShoppingListObserver {
    override fun onItemAdded(item: Item) {
        println("Новий товар додано: ${item.name} (${item.quantity} x ${item.price})")
    }

    override fun onItemRemoved(item: Item) {
        println("Товар видалено: ${item.name}")
    }
}

fun main() {
    val manager = ShoppingListManager.instance

    val shoppingList = manager.createList("Мій чек")


    val notifier = ShoppingListNotifier()
    shoppingList.addObserver(notifier)

    println("Додаємо товари...")
    shoppingList.addItem(Item(0, "Молоко", 2, 25.0))
    shoppingList.addItem(Item(0, "Хліб", 1, 15.0))

    // Використання адаптера для зовнішнього товару
    val externalItem = ExternalItem("ext123", "Яблука", 5, 12.0)
    val adaptedItem = ItemAdapter(externalItem).toItem(shoppingList.getNextId())
    shoppingList.addItem(adaptedItem)

    println("\nВидаляємо товар ID 1...")
    shoppingList.removeItem(1)

    println("\nПоточний список:")
    shoppingList.displayItems()

    println("\nДрукуємо фінальний чек:")
    shoppingList.printReceipt()
}
