package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.VariableDataTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure dataType matches the actual value type.
 *
 * <p>This annotation validates that: - If dataType is NUMBER, value should be a valid integer - If
 * dataType is DECIMAL, value should be a valid double - If dataType is BOOL, value should be
 * true/false - If dataType is STRING, value should be a string
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VariableDataTypeValidator.class)
public @interface ValidVariableDataType {
  String message() default "DataType does not match the value type";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
