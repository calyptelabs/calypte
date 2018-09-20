/*
 * Calypte http://calypte.uoutec.com.br/
 * Copyright (C) 2018 UoUTec. (calypte@uoutec.com.br)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package calypte.tx;

/**
 * Representa uma transação em um cache.
 * @author Ribeiro
 *
 */
public interface CacheTransaction {

	/**
	 * Verifica se todas as operações contidas na transação foram desfeitas.
	 * @return <code>true</code> se foram desfeitas. Caso contrário, <code>false</code>
	 */
	boolean isRolledBack();
	
	/**
	 * Verifica se todas as operações contidas na transação foram confirmadas.
	 * @return <code>true</code> se foram confirmadas. Caso contrário, <code>false</code>
	 */
	boolean isCommited();
	
	/**
	 * Desfaz todas as operações contidas na transação.
	 */
	void rollback() throws TransactionException;
	
	/**
	 * Confirma todas as operações contidas na transação.
	 */
	void commit() throws TransactionException;
	
}
