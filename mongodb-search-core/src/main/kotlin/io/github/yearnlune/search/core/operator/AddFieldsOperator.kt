package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.graphql.GroupByOptionType
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.schema.JsonSchemaObject

class AddFieldsOperator(
    fields: List<Field>
) : AggregateOperator() {

    override var operation: AggregationOperation? = null

    init {
        var addFieldsOperation = Aggregation.addFields().build()

        fields.forEach {
            addFieldsOperation = addFields(addFieldsOperation, it)
        }

        this.operation = addFieldsOperation
    }

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return this.operation!!
    }

    private fun addFields(addFieldOperation: AddFieldsOperation, field: Field): AddFieldsOperation {
        val date = ConvertOperators.Convert.convertValueOf(field.key).to(JsonSchemaObject.Type.dateType())
        val format = when (field.option) {
            GroupByOptionType.DAILY -> "%Y%m%d"
            GroupByOptionType.WEEKLY -> "%Y%V"
            GroupByOptionType.MONTHLY -> "%Y%m"
            GroupByOptionType.YEARLY -> "%Y"
            else -> throw IllegalArgumentException()
        }
        return addFieldOperation.addField(field.alias, DateOperators.DateToString.dateOf(date).toString(format))
    }

    data class Field(
        val key: String,
        val option: GroupByOptionType,
        val alias: String
    )
}