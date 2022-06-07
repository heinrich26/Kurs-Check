package data

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter


class ListOfPairConverter : Converter<List<Pair<*, *>>, Map<*, *>> {
    override fun convert(value: List<Pair<*, *>>): Map<*, *> = value.toMap()

    override fun getInputType(typeFactory: TypeFactory): JavaType =
        typeFactory.constructParametricType(
            List::class.java,
            typeFactory.constructParametricType(Pair::class.java, Any::class.java, Any::class.java)
        )

    override fun getOutputType(typeFactory: TypeFactory): JavaType =
        typeFactory.constructMapType(Map::class.java, Any::class.java, Any::class.java)
}
