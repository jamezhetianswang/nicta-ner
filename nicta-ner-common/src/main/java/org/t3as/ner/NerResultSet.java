/*
 * #%L
 * NICTA t3as Named-Entity Recognition library
 * %%
 * Copyright (C) 2010 - 2014 NICTA
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.t3as.ner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * This class encapsulated the two result set used in the NameExtractor class
 * into one single class.
 * <p/>
 * The map result can be got from this class as well.
 */
public class NerResultSet {

    public final List<List<Token>> tokens;
    public final List<List<Phrase>> phrases;

    @JsonCreator
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public NerResultSet(@JsonProperty("phrases") final List<List<Phrase>> phrases,
                        @JsonProperty("tokens") final List<List<Token>> tokens) {
        this.phrases = phrases;
        this.tokens = tokens;
    }

    /** This method returns a map format set of the result. */
    @JsonIgnore
    public Map<EntityClass, Set<String>> getMappedResult() {
        final Map<EntityClass, Set<String>> m = new HashMap<>();
        for (final List<Phrase> pa : phrases) {
            for (final Phrase p : pa) {
                Set<String> c = m.get(p.phraseType);
                if (c == null) {
                    c = new HashSet<>();
                    m.put(p.phraseType, c);
                }
                c.add(p.phraseString());
            }
        }
        return m;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (int si = 0; si < tokens.size(); si++) {
            final List<Token> sentence = tokens.get(si);
            final List<Phrase> phraseList = this.phrases.get(si);
            for (final Token aSentence : sentence) sb.append(aSentence.text).append(" ");
            sb.append("\n===============================================\n");
            for (final Phrase p : phraseList) {
                String ptext = "";
                for (int wi = 0; wi < p.phrase.size(); wi++) {
                    ptext += (p.phrase.get(wi).text + " ");
                }
                ptext = ptext.trim();

                final StringBuilder stext = new StringBuilder();
                for (final Map.Entry<EntityClass, Double> e : p.score.entrySet()) {
                    if (stext.length() != 0) stext.append(", ");
                    stext.append(e.getKey()).append(":").append(e.getValue());
                }

                // what we are trying to generate:
                // 0: John	PERSON	11.25, 40.0, -10.0	null	0:0:0:1:1

                // 0: John\t
                sb.append(format("%s: %s\t", p.phrasePosition, ptext));
                // PERSON\t
                sb.append(format("%s\t", p.phraseType));
                // 11.25, 40.0, -10.0\t
                sb.append(format("%s\t", stext));
                // null\t
                sb.append(format("%s\t", p.attachedWordMap.get("prep")));
                // 0:0:1:1\n
                sb.append(format("%d:%d:%d:%d:%d\n", p.phrase.iterator().next().startIndex, p.phrasePosition,
                                 p.phraseStubPosition, p.phraseStubLength, p.phraseLength));
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
