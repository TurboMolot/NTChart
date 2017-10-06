package ru.turbomolot.ntchart.formatter;

import ru.turbomolot.ntchart.axis.IAxis;
import ru.turbomolot.ntchart.series.ISeries;

/**
 * Created by TurboMolot on 04.10.17.
 */

public interface IFormatterValue {
    String formatText(float val, IAxis axis);
}
