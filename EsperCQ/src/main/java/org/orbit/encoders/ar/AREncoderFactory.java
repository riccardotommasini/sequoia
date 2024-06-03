package org.orbit.encoders.ar;

import org.orbit.Parameters.EncodingTypeContradictionPart;
import org.orbit.Parameters.RepairType;
import org.orbit.encoders.SATEncoder;
import org.orbit.encoders.SATEncoderFactory;


public class AREncoderFactory extends SATEncoderFactory {

	public AREncoderFactory(RepairType repair, EncodingTypeContradictionPart encoding) {
		super(repair, encoding);
	}

	@Override
	public SATEncoder createEncoder() {
		switch(repairType) {
		case standard:
			switch(encodingContradiction) {
			case cqapri_encoding:
				return new ARNoPrioCqapriEncoder(answersFilter);
			case cavsat_encoding:
				return new ARNoPrioCavsatEncoder(answersFilter);
			default:
				System.out.println("unsupported encoding type");
				break;
			}
			break;
		case pareto_all_reachable_encoding:			
			switch(encodingContradiction) {
			case cqapri_encoding:
				return new ARScoreStructuredOptiCqapriEncoder(answersFilter);
			case cavsat_encoding:
				return new ARScoreStructuredOptiCavsatEncoder(answersFilter);
			default:
				System.out.println("unsupported encoding type");
				break;
			}
			break;
		case pareto_conf_of_conf_encoding:
			switch(encodingContradiction) {
			case cqapri_encoding:
				return new ARParetoCqapriEncoder(answersFilter);
			case cavsat_encoding:
				return new ARParetoCavsatEncoder(answersFilter);
			default:
				System.out.println("unsupported encoding type");
				break;
			}
			break;
		case completion:
			switch(encodingContradiction) {
			case cqapri_encoding:
				return new ARCompletionCqapriEncoder(answersFilter);
			case cavsat_encoding:
				return new ARCompletionCavsatEncoder(answersFilter);
			default:
				System.out.println("unsupported encoding type");
				break;
			}
			break;
		default:
			System.out.println("unsupported repair type");
			break;
		}
		return null;
	}	
}
