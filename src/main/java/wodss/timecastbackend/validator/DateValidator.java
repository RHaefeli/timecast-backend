package wodss.timecastbackend.validator;

import wodss.timecastbackend.util.PreconditionFailedException;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidator implements
        ConstraintValidator<DateConstraint, String> {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Override
    public void initialize(DateConstraint customDate) {
    }

    @Override
    public boolean isValid(String customDateField,
                           ConstraintValidatorContext cxt) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try
        {
            sdf.setLenient(false);
            Date d = sdf.parse(customDateField);
            return true;
        }
        catch (ParseException e)
        {
            return false;
        }
    }

}