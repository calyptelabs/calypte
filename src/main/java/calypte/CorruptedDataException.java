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
 * Exceção lançada quando os dados de uma entrada estão inconsistentes.
 * 
 * @author Ribeiro
 */
public class CorruptedDataException extends RecoverException{

	private static final long serialVersionUID = 4887368633482097027L;

	public CorruptedDataException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CorruptedDataException(CacheError error, Object... params) {
		super(error, params);
		// TODO Auto-generated constructor stub
	}

	public CorruptedDataException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CorruptedDataException(Throwable thrwbl, CacheError error,
			Object... params) {
		super(thrwbl, error, params);
		// TODO Auto-generated constructor stub
	}
    

}
