import javax.swing.SpinnerNumberModel
import kotlin.math.max

class ChainedSpinnerNumberModel(value: Int, min: Int, max: Int, val next: ChainedSpinnerNumberModel? = null) :
    SpinnerNumberModel(value, min, max, 1) {
    init {
        if (next != null) {
            next.minimum = number
            addChangeListener {
                next.value = max(next.value as Int, number)
                next.minimum
            }
        }
    }

    override fun getNumber(): Int {
        return super.getNumber() as Int
    }

}