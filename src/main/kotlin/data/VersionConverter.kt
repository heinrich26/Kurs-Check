package data

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class VersionConverter : Converter<Pair<Int, Int>, String> {
    override fun convert(value: Pair<Int, Int>?): String = "$value.first.$value.second"

    override fun getInputType(typeFactory: TypeFactory): JavaType =
        typeFactory.constructParametricType(Pair::class.java, Int::class.java, Int::class.java)

    override fun getOutputType(typeFactory: TypeFactory): JavaType = typeFactory.constructType(String::class.java)

}
