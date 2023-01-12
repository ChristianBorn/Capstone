import axios from 'axios';
import React, {ChangeEvent, useEffect, useState} from 'react';
import Modal from 'react-modal';
import {HorseModel} from "./HorseModel";
import FieldLabelGroup from "../structuralComponents/FieldLabelGroup";
import CloseIcon from "../icons/CloseIcon";
import "../index/css/AddItemModal.css";
import "../buttons/css/SubmitButton.css";

type ModalProps = {
    modalIsOpen: boolean,
    closeModal: () => void,
    reloadHorses: () => void,
    setSuccessMessage: (input: string) => void,
}

function AddHorseModal(props: ModalProps) {
    const [errorMessages, setErrorMessages] = useState({
        name: "", owner: ""
    })
    const [newHorse, setNewHorse] = useState<HorseModel>({
        id: "", name: "", owner: {id: "", name: "", ownsHorse: []}, consumptionList: []
    })
    useEffect(() => {
        setErrorMessages({name: "", owner: ""})
    }, [props.closeModal])

    const handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault()
        axios.post("/horses/", newHorse)
            .catch((e) => {
                if (e.response.status === 400) {
                    setErrorMessages({
                        ...errorMessages,
                        [e.response.data.fieldName]: e.response.data.errorMessage
                    })
                }
                console.error("POST Error: " + e)
                throw e
            })
            .then(props.reloadHorses)
            .then(props.closeModal)
            .then(() => setNewHorse({id: "", name: "", owner: {id: "", name: "", ownsHorse: []}, consumptionList: []}))
            .then(() => props.setSuccessMessage("Eintrag erfolgreich hinzugefügt"))
    }
    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const {name, value} = event.target
        setNewHorse({
            ...newHorse,
            [name]: value
        })
    }

    return (
        <Modal
            isOpen={props.modalIsOpen}
            contentLabel="Add Modal"
            ariaHideApp={false}
            onRequestClose={props.closeModal}>
            <CloseIcon closeModal={props.closeModal}/>
            <section>
                <h2>Neues Pferd anlegen</h2>
                <form onSubmit={handleSubmit}>

                    <FieldLabelGroup>
                        <label htmlFor={"name"}>Name</label>
                        <input onChange={handleChange} required type={"text"} id={"name"} name={"name"}/>
                        {errorMessages.name &&
                            <div className={"message-container"}><p
                                className={"error-message"}>{errorMessages.name}</p></div>}
                    </FieldLabelGroup>

                    <div className={"button-group"}>
                        <button className={"submit-button"} type={"submit"}>Speichern</button>
                        <button className={"abort-button"} onClick={props.closeModal}>Abbrechen</button>
                    </div>
                </form>
            </section>
        </Modal>
    );
}

export default AddHorseModal;