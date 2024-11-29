package pl.edu.agh.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object OrderTable: IntIdTable() {
    val company = reference("company", CompanyTable, fkName = "FK_Order_Company_Id")
    val client = reference("client", UserTable, fkName = "FK_Order_Client_Id")
    val name = varchar("name", 20).nullable()
    val sendOn = datetime("send_on").nullable()
    val placedOn = datetime("placed_on")
    val deliveredOn = datetime("delivered_on").nullable()
    val expectedDeliveryOn = datetime("expected_delivery").nullable()
    val courier = reference("courier", UserTable, fkName = "FK_Order_Courier_Id").nullable()
    val totalPrice = decimal("total_price", 10, 2)
}

object OrderProductTable : IntIdTable("order_product") {
    val order = reference("order", OrderTable, fkName = "FK_OrderProduct_Order_Id")
    val product = reference("product", ProductTable, fkName = "FK_OrderProduct_Product_Id")
    val quantity = integer("quantity")
    init {
        check("quantity_not_negative") { quantity greaterEq 0 }
        uniqueIndex("order_product_unique", order, product)
    }
}

class OrderDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderDAO>(OrderTable)

    var companyDAO by CompanyDAO referencedOn OrderTable.company
    var client by UserDAO referencedOn OrderTable.client
    var name by OrderTable.name
    var sendOn by OrderTable.sendOn
    var placedOn by OrderTable.placedOn
    var deliveredOn by OrderTable.deliveredOn
    var expectedDeliveryOn by OrderTable.expectedDeliveryOn
    var courier by UserDAO optionalReferencedOn OrderTable.courier
    var totalPrice by OrderTable.totalPrice
    private val orderProductsDAO by OrderProductDAO referrersOn OrderProductTable.order

    val products: Map<ProductDAO, Int> get() {
        return orderProductsDAO.associate { it.productDAO to it.quantity }
    }
}

class OrderProductDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderProductDAO>(OrderProductTable)

    var orderDAO by OrderDAO referencedOn OrderProductTable.order
    var productDAO by ProductDAO referencedOn OrderProductTable.product
    var quantity by OrderProductTable.quantity
}