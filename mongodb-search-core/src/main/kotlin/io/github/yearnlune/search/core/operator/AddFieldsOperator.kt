package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.graphql.GroupByOptionInput
import io.github.yearnlune.search.graphql.GroupByOptionType
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.aggregation.DateOperators
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
        return if (field.isDateOption()) {
            addDateField(addFieldOperation, field)
        } else {
            when (field.option) {
                GroupByOptionType.EXISTS -> addFieldOperation.addField(
                    field.alias,
                    ExistsOperator(field.key, listOf(true)).buildExpression()
                )
                else -> throw UnsupportedOperationException()
            }
        }
    }

    private fun processTimezone(field: Field): ConvertOperators.Convert {
        val date = ConvertOperators.Convert.convertValueOf(field.key).to(JsonSchemaObject.Type.dateType())
        val timestamp = ConvertOperators.Convert.convertValueOf(date).to(JsonSchemaObject.Type.longType())
        val timestampWithTimezone =
            ArithmeticOperators.Add.valueOf(timestamp).add((field.options?.timezone ?: 0) * 3600)
        return ConvertOperators.Convert.convertValueOf(timestampWithTimezone).to(JsonSchemaObject.Type.dateType())
    }

    private fun addDateField(addFieldOperation: AddFieldsOperation, field: Field): AddFieldsOperation {
        val dateWithTimezone = processTimezone(field)
        val format = when (field.option ?: field.options?.type) {
            GroupByOptionType.DAILY -> "%Y%m%d"
            GroupByOptionType.WEEKLY -> "%Y%V"
            GroupByOptionType.MONTHLY -> "%Y%m"
            GroupByOptionType.YEARLY -> "%Y"
            else -> throw IllegalArgumentException()
        }
        return addFieldOperation.addField(
            field.alias,
            DateOperators.DateToString.dateOf(dateWithTimezone).toString(format)
        )
    }

    data class Field(
        val key: String,
        val option: GroupByOptionType? = null,
        val options: GroupByOptionInput? = null,
        val alias: String
    ) {

        fun isDateOption() = (option != null && option !== GroupByOptionType.EXISTS) ||
            (options != null && options.type !== GroupByOptionType.EXISTS)
    }
}