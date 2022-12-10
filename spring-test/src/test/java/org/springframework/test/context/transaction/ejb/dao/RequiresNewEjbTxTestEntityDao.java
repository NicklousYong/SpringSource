/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.transaction.ejb.dao;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * EJB implementation of {@link TestEntityDao} which declares transaction
 * semantics for {@link #incrementCount(String)} with
 * {@link TransactionAttributeType#REQUIRES_NEW}.
 *
 * @author Sam Brannen
 * @author Xavier Detant
 * @see RequiredEjbTxTestEntityDao
 * @since 4.0.1
 */
@Stateless
@Local(TestEntityDao.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class RequiresNewEjbTxTestEntityDao extends AbstractEjbTxTestEntityDao {

	@Override
	public int getCount(String name) {
		return super.getCountInternal(name);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public int incrementCount(String name) {
		return super.incrementCountInternal(name);
	}

}