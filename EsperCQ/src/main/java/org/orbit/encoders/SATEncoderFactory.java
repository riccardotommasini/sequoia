package org.orbit.encoders;

import org.orbit.filters.AnswersFilter;

import java.util.Set;

import static org.orbit.Parameters.*;

public abstract class SATEncoderFactory {
    protected AnswersFilter answersFilter;
    protected Set<String> setOfAnswersToCheck;
    protected RepairType repairType;
    protected EncodingTypeContradictionPart encodingContradiction;

    public SATEncoderFactory(RepairType repair, EncodingTypeContradictionPart encoding) {
        repairType = repair;
        encodingContradiction = encoding;
    }

    public void setFilter(AnswersFilter filter) {
        answersFilter = filter;
    }

    public abstract SATEncoder createEncoder();

}
