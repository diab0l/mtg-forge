/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.card;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import forge.util.IItemReader;
import forge.util.storage.StorageBase;
import forge.util.storage.StorageReaderBase;

public final class EditionCollection extends StorageBase<CardEdition> {

    private final Map<String, CardEdition> aliasToEdition = new TreeMap<String, CardEdition>(String.CASE_INSENSITIVE_ORDER);

    public EditionCollection(IItemReader<CardEdition> reader) {
        super("Card editions", reader);

        for (CardEdition ee : this) {
            String alias = ee.getAlias();
            if (null != alias) {
                aliasToEdition.put(alias, ee);
            }
            aliasToEdition.put(ee.getCode2(), ee);
        }
    }

    /**
     * Gets a sets by code.  It will search first by three letter codes, then by aliases and two-letter codes.
     * 
     * @param code
     *            the code
     * @return the sets the by code
     */
    @Override
    public CardEdition get(final String code) {
        CardEdition baseResult = super.get(code);
        return baseResult == null ? aliasToEdition.get(code) : baseResult;
    }
    
    
    public Iterable<CardEdition> getOrderedEditions() {
        List<CardEdition> res = Lists.newArrayList(this);
        Collections.sort(res);
        Collections.reverse(res);
        return res;
    }

    /**
     * Gets the sets by code or throw.
     * 
     * @param code
     *            the code
     * @return the sets the by code or throw
     */
    public CardEdition getEditionByCodeOrThrow(final String code) {
        final CardEdition set = this.get(code);
        if (null == set) {
            throw new RuntimeException(String.format("Edition with code '%s' not found", code));
        }
        return set;
    }

    // used by image generating code
    /**
     * Gets the code2 by code.
     * 
     * @param code
     *            the code
     * @return the code2 by code
     */
    public String getCode2ByCode(final String code) {
        final CardEdition set = this.get(code);
        return set == null ? "" : set.getCode2();
    }

    public final Function<String, CardEdition> FN_EDITION_BY_CODE = new Function<String, CardEdition>() {
        @Override
        public CardEdition apply(String code) {
            return EditionCollection.this.get(code);
        };
    };

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public IItemReader<SealedProductTemplate> getBoosterGenerator() {
        // TODO Auto-generated method stub
        return new StorageReaderBase<SealedProductTemplate>(null) {
            
            @Override
            public Map<String, SealedProductTemplate> readAll() {
                Map<String, SealedProductTemplate> map = new TreeMap<String, SealedProductTemplate>(String.CASE_INSENSITIVE_ORDER);
                for(CardEdition ce : EditionCollection.this) {
                     map.put(ce.getCode(), ce.getBoosterTemplate());
                }
                return map;
            }
            
            @Override
            public String getItemKey(SealedProductTemplate item) {
                return item.getEdition();
            }
        };
    }
}

