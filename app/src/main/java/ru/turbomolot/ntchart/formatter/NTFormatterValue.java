package ru.turbomolot.ntchart.formatter;

import java.text.DecimalFormat;

import ru.turbomolot.ntchart.axis.IAxis;
import ru.turbomolot.ntchart.series.ISeries;

/**
 * Created by XE on 01.10.2017.
 */

public class NTFormatterValue implements IFormatterValue {
    protected DecimalFormat format;
    protected int digits = 0;

    public NTFormatterValue(int digits) {
        this.digits = digits;
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                b.append(".");
            b.append("0");
        }

        format = new DecimalFormat("###,###,###,##0" + b.toString());
    }

    public NTFormatterValue() {
        this(2);
    }

    @Override
    public String formatText(float val, IAxis axis) {
        return format.format(val);
    }
}
