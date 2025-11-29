package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.experiment.WinningVariant;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Map;

/**
 * Validates winning_variant constraints.
 *
 * <ul>
 *   <li>Can ONLY be set when status is transitioning to CONCLUDED
 *   <li>When concluding, winning_variant is required
 *   <li>winning_variant.variantName must exist in variants
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class WinningVariantValidationRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus newStatus = request.getStatus();
    WinningVariant winningVariant = request.getWinningVariant();

    boolean isTransitioningToConcluded = newStatus == ExperimentStatus.CONCLUDED;

    if (winningVariant != null && !isTransitioningToConcluded) {
      throw new RestException(ErrorEnum.WINNING_VARIANT_ONLY_ON_CONCLUDE);
    }

    if (isTransitioningToConcluded) {
      if (winningVariant == null
          || winningVariant.getVariantName() == null
          || winningVariant.getVariantName().isBlank()) {
        throw new RestException(ErrorEnum.WINNING_VARIANT_REQUIRED);
      }

      Map<String, Variant> existingVariants = existing.getVariants();
      if (existingVariants == null || existingVariants.isEmpty()) {
        throw new RestException(ErrorEnum.WINNING_VARIANT_INVALID);
      }

      if (!existingVariants.containsKey(winningVariant.getVariantName())) {
        throw new RestException(ErrorEnum.WINNING_VARIANT_INVALID);
      }
    }
  }
}
