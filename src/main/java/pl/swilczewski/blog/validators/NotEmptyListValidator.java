package pl.swilczewski.blog.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class NotEmptyListValidator implements ConstraintValidator<NotEmptyList, List<String>> {
    public void initialize(NotEmptyList constraint) {
    }

    @Override
    public boolean isValid(List<String> list, ConstraintValidatorContext constraintValidatorContext) {
        return list != null && !list.isEmpty();
    }
}
