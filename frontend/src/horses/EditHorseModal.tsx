import axios from 'axios';
import React, {useEffect, useState} from 'react';
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
    horseToEdit: HorseModel
}

function EditHorseModal(props: ModalProps) {
    const [errorMessages, setErrorMessages] = useState({
        name: "", owner: ""
    })
    const [editedHorse, setEditedHorse] = useState<HorseModel>(props.horseToEdit)
    useEffect(() => setEditedHorse(props.horseToEdit), [props.horseToEdit])
    useEffect(() => {
        setErrorMessages({name: "", owner: ""})
    }, [props.closeModal])

    const saveNewHorse = () => {
        axios.put("/horses/", editedHorse)
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
            .then(() => setEditedHorse(props.horseToEdit))
            .then(() => props.setSuccessMessage("Eintrag erfolgreich geändert"))
    }

    const handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault()
        saveNewHorse()
    }
    const handleChange = (event: any) => {
        const name = event.target.name;
        const value = event.target.value;
        setEditedHorse({
            ...editedHorse,
            [name]: value
        })
    }

    return (
        <Modal
            isOpen={props.modalIsOpen}
            contentLabel="Edit Modal"
            ariaHideApp={false}
            onRequestClose={props.closeModal}>
            <CloseIcon closeModal={props.closeModal}/>
            <section>
                <h2>Pferd bearbeiten</h2>
                <form onSubmit={handleSubmit}>

                    <FieldLabelGroup>
                        <label htmlFor={"name"}>Name</label>
                        <input value={editedHorse.name} onChange={handleChange} required type={"text"} id={"name"}
                               name={"name"}/>
                        {errorMessages.name &&
                            <div className={"message-container"}><p
                                className={"error-message"}>{errorMessages.name}</p></div>}
                    </FieldLabelGroup>

                    <div className={"button-group"}>
                        <button className={"submit-button"} type={"submit"}>Änderungen speichern</button>
                    </div>
                </form>
            </section>
        </Modal>
    );
}

export default EditHorseModal;
