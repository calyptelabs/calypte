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

package calypte;

/**
 * Descreve um erro no cache.
 *  
 * @author Ribeiro
 *
 */
public class CacheError {

	private final int id;
	
	private final String message;

	/**
	 * Cria um novo erro com uma identificação e mensagem específica.
	 * @param id identificação.
	 * @param message mensagem.
	 */
	public CacheError(int id, String message) {
		this.id = id;
		this.message = message;
	}

	/**
	 * Obtém a identificação.
	 * @return identificação.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Obtém a mensagem do erro.
	 * @return mensagem.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Obtém a descrição do erro.
	 * @param params
	 * @return Descrição.
	 */
	public String getString(Object ... params){
		return "ERROR " + this.id + ": " + String.format(this.message, (Object[])params);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CacheError other = (CacheError) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
