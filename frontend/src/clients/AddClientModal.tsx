import axios from 'axios';
import React, {ChangeEvent, useEffect, useState} from 'react';
import Modal from 'react-modal';
import {ClientModel} from "./ClientModel";
import FieldLabelGroup from "../structuralComponents/FieldLabelGroup";
import CloseIcon from "../icons/CloseIcon";
import "../index/css/AddItemModal.css";
import "../buttons/css/SubmitButton.css";
import {HorseModel} from "../horses/HorseModel";
import Select from "react-select";

type ModalProps = {
    modalIsOpen: boolean,
    closeModal: () => void,
    horseList: HorseModel[],
    registeredClients: ClientModel[],
    reloadClients: () => void,
    setSuccessMessage: (input: string) => void,
}

function AddClientModal(props: ModalProps) {
    const [newClient, setNewClient] = useState<ClientModel>(
        {id: "", name: "", ownsHorse: []})
    const [horseSelectList, setHorseSelectList] = useState<{}[]>([])
    const [clients, setClients] = useState<ClientModel[]>([])
    const [selectContent, setSelectContent] = useState<{ label: string, value: HorseModel }[]>()


    useEffect(() => {
        const newHorseSelectList: {}[] = []
        let horsesWithOwners: {}[] = []
        setClients(props.registeredClients)
        clients.map(client => client.ownsHorse
            .map(horse => horsesWithOwners.push(horse.id)))

        props.horseList.forEach(horse => {
            if (!horsesWithOwners.includes(horse.id)) {
                newHorseSelectList.push(
                    {"label": horse.name, "value": horse}
                )
            }
        })
        setHorseSelectList(newHorseSelectList)
        // eslint-disable-next-line
    }, [props.horseList, props.registeredClients])

    const handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault()
        selectContent?.map(selectedOption => newClient.ownsHorse.push(selectedOption.value))
        console.log(newClient)
        axios.put("/clients/", newClient)
            .catch((e) => console.error("POST Error: " + e))
            .then(props.reloadClients)
            .then(props.closeModal)
            .then(() => setNewClient({id: "", name: "", ownsHorse: []}))
            .then(() => props.setSuccessMessage("Eintrag erfolgreich hinzugefügt"))
    }
    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const {name, value} = event.target
        setNewClient({
            ...newClient,
            [name]: value
        })
    }
    const handleSelectChange = (event: any) => {
        setSelectContent(event)
    }

    return (
        <Modal
            isOpen={props.modalIsOpen}
            contentLabel="Add Modal"
            ariaHideApp={false}
            onRequestClose={props.closeModal}>
            <CloseIcon closeModal={props.closeModal}/>
            <section>
                <h2>Neuen Einstaller anlegen</h2>
                <form onSubmit={handleSubmit}>

                    <FieldLabelGroup>
                        <label htmlFor={"name"}>Name</label>
                        <input onChange={handleChange} required type={"text"} id={"name"} name={"name"}/>
                    </FieldLabelGroup>

                    <FieldLabelGroup>
                        <label htmlFor={"owns"}>Besitzt</label>
                        <Select isMulti options={horseSelectList} onChange={handleSelectChange} required id={"owns"}
                                name={"owns"}/>
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

export default AddClientModal;