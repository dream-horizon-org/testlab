package com.ascend.testlab.validation;

import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.validation.annotations.ValidVariantStructure;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Constraint validator for ValidVariantStructure annotation.
 *
 * <p>This validator checks that variant structure updates only modify variable values. It requires
 * existing experiment data to be available in the validation context.
 *
 * <p>Note: The actual validation logic is delegated to VariantStructureValidator which has access
 * to ObjectMapper and can properly deserialize JSONB data.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class VariantStructureConstraintValidator
    implements ConstraintValidator<ValidVariantStructure, Map<String, Variant>> {

  @Override
  public void initialize(ValidVariantStructure constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(Map<String, Variant> variants, ConstraintValidatorContext context) {

    if (variants == null || variants.isEmpty()) {
      return true; // Null/empty is valid, other validators will handle required checks
    }

    // Note: This validator cannot access existing experiment data from the database
    // The actual validation is performed in the service layer where we have access to existing data
    // This annotation serves as a marker and documentation that this field has structure validation

    log.debug("Variant structure validation will be performed in service layer");
    return true;
  }
}
