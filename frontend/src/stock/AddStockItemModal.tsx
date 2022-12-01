import axios from 'axios';
import React, {useState} from 'react';
import Modal from 'react-modal';
import {StockItemModel} from "./StockItemModel";
import Form3Rows from "../structuralComponents/Form3Rows";
import FieldLabelGroup from "../structuralComponents/FieldLabelGroup";
import CloseIcon from "../icons/CloseIcon";
import "../index/css/AddItemModal.css";
import "../buttons/css/SubmitButton.css";

type ModalProps = {
    modalIsOpen: boolean,
    closeModal: () => void,
    reloadStockItems: () => void,
    setSuccessMessage: (input: string) => void,
}

function AddItemModal(props: ModalProps) {
    const [newStockItem, setNewStockItem] = useState<StockItemModel>({
        id: "", name: "", amountInStock: 0, pricePerKilo: 0, type: ""
    })


    const saveNewStockitem = () => {
        axios.post("/stock/", newStockItem)
            .catch((e) => console.error("POST Error: " + e))
            .then(props.reloadStockItems)
            .then(props.closeModal)
            .then(() => setNewStockItem({id: "", name: "", amountInStock: 0, pricePerKilo: 0, type: ""}))
            .then(() => props.setSuccessMessage("Eintrag erfolgreich hinzugefügt"))

    }

    const handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault()
        saveNewStockitem()
    }
    const handleChange = (event: any) => {
        const name = event.target.name;
        const value = event.target.value;
        setNewStockItem({
            ...newStockItem,
            [name]: value
        })
    }


    return (
        <Modal
            isOpen={props.modalIsOpen}
            contentLabel="Add Modal"
            ariaHideApp={false}
            onRequestClose={props.closeModal}
        >
            <CloseIcon closeModal={props.closeModal}/>
            <section>
                <h2>Neue Position dem Lager hinzufügen</h2>
                <form onSubmit={handleSubmit}>

                    <FieldLabelGroup>
                        <label htmlFor={"name"}>Name/Bezeichnung</label>
                        <input onChange={handleChange} required type={"text"} id={"name"} name={"name"}/>
                    </FieldLabelGroup>

                    <Form3Rows>
                        <FieldLabelGroup>
                            <label htmlFor={"price"}>Preis pro <abbr title={"Kilogramm"}>kg</abbr></label>
                            <input onChange={handleChange} placeholder={"0"} step={"0.1"} min={"0"} required
                                   type={"number"}
                                   id={"price"} name={"pricePerKilo"}/>
                        </FieldLabelGroup>
                        <FieldLabelGroup>
                            <label htmlFor={"amount"}>Menge in <abbr title={"Kilogramm"}>kg</abbr></label>
                            <input onChange={handleChange} placeholder={"0"} step={"0.1"} min={"0"} required
                                   type={"number"}
                                   id={"amount"} name={"amountInStock"}/>
                        </FieldLabelGroup>
                        <FieldLabelGroup>
                            <label htmlFor={"type"}>Typ</label>
                            <select onChange={handleChange} required id={"type"} name={"type"}>
                                <option value="" selected disabled hidden>Bitte auswählen</option>
                                <option value={"Futter"}>Futter</option>
                                <option value={"Einstreu"}>Einstreu</option>
                            </select>
                        </FieldLabelGroup>
                    </Form3Rows>
                    <div className={"button-group"}>
                        <button className={"submit-button"} type={"submit"}>Einlagern</button>
                    </div>
                </form>
            </section>
        </Modal>
    );
}

export default AddItemModal;
