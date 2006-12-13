/**
 * <copyright>
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * </copyright>
 *
 * $Id: ConditionImpl.java,v 1.2 2006/12/13 16:17:31 smoser Exp $
 */
package org.eclipse.bpel.model.impl;

import javax.xml.namespace.QName;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.Condition;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.w3c.dom.Element;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Condition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class ConditionImpl extends ExpressionImpl implements Condition {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ConditionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return BPELPackage.eINSTANCE.getCondition();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case BPELPackage.CONDITION__DOCUMENTATION_ELEMENT:
				return getDocumentationElement();
			case BPELPackage.CONDITION__ELEMENT:
				return getElement();
			case BPELPackage.CONDITION__REQUIRED:
				return isRequired() ? Boolean.TRUE : Boolean.FALSE;
			case BPELPackage.CONDITION__ELEMENT_TYPE:
				return getElementType();
			case BPELPackage.CONDITION__BODY:
				return getBody();
			case BPELPackage.CONDITION__EXPRESSION_LANGUAGE:
				return getExpressionLanguage();
			case BPELPackage.CONDITION__OPAQUE:
				return getOpaque();
		}
		return eDynamicGet(eFeature, resolve);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(EStructuralFeature eFeature, Object newValue) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case BPELPackage.CONDITION__DOCUMENTATION_ELEMENT:
				setDocumentationElement((Element)newValue);
				return;
			case BPELPackage.CONDITION__ELEMENT:
				setElement((Element)newValue);
				return;
			case BPELPackage.CONDITION__REQUIRED:
				setRequired(((Boolean)newValue).booleanValue());
				return;
			case BPELPackage.CONDITION__ELEMENT_TYPE:
				setElementType((QName)newValue);
				return;
			case BPELPackage.CONDITION__BODY:
				setBody((Object)newValue);
				return;
			case BPELPackage.CONDITION__EXPRESSION_LANGUAGE:
				setExpressionLanguage((String)newValue);
				return;
			case BPELPackage.CONDITION__OPAQUE:
				setOpaque((Boolean)newValue);
				return;
		}
		eDynamicSet(eFeature, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(EStructuralFeature eFeature) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case BPELPackage.CONDITION__DOCUMENTATION_ELEMENT:
				setDocumentationElement(DOCUMENTATION_ELEMENT_EDEFAULT);
				return;
			case BPELPackage.CONDITION__ELEMENT:
				setElement(ELEMENT_EDEFAULT);
				return;
			case BPELPackage.CONDITION__REQUIRED:
				setRequired(REQUIRED_EDEFAULT);
				return;
			case BPELPackage.CONDITION__ELEMENT_TYPE:
				setElementType(ELEMENT_TYPE_EDEFAULT);
				return;
			case BPELPackage.CONDITION__BODY:
				setBody(BODY_EDEFAULT);
				return;
			case BPELPackage.CONDITION__EXPRESSION_LANGUAGE:
				unsetExpressionLanguage();
				return;
			case BPELPackage.CONDITION__OPAQUE:
				unsetOpaque();
				return;
		}
		eDynamicUnset(eFeature);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(EStructuralFeature eFeature) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case BPELPackage.CONDITION__DOCUMENTATION_ELEMENT:
				return DOCUMENTATION_ELEMENT_EDEFAULT == null ? documentationElement != null : !DOCUMENTATION_ELEMENT_EDEFAULT.equals(documentationElement);
			case BPELPackage.CONDITION__ELEMENT:
				return ELEMENT_EDEFAULT == null ? element != null : !ELEMENT_EDEFAULT.equals(element);
			case BPELPackage.CONDITION__REQUIRED:
				return required != REQUIRED_EDEFAULT;
			case BPELPackage.CONDITION__ELEMENT_TYPE:
				return ELEMENT_TYPE_EDEFAULT == null ? elementType != null : !ELEMENT_TYPE_EDEFAULT.equals(elementType);
			case BPELPackage.CONDITION__BODY:
				return BODY_EDEFAULT == null ? body != null : !BODY_EDEFAULT.equals(body);
			case BPELPackage.CONDITION__EXPRESSION_LANGUAGE:
				return isSetExpressionLanguage();
			case BPELPackage.CONDITION__OPAQUE:
				return isSetOpaque();
		}
		return eDynamicIsSet(eFeature);
	}

} //ConditionImpl
