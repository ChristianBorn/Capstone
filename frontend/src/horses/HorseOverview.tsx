import React, {useEffect, useState} from 'react';
import axios from "axios";
import {HorseModel} from "./HorseModel";
import {BounceLoader} from "react-spinners";
import AddIcon from "../icons/AddIcon";
import AddHorseModal from "./AddHorseModal";
import DeleteIcon from "../icons/DeleteIcon";
import DeleteHorseModal from "./DeleteHorseModal";
import EditHorseModal from "./EditHorseModal";
import EditIcon from "../icons/EditIcon";
import {StockItemModel} from "../stock/StockItemModel";
import AddConsumptionModal from "./AddConsumptionModal";
import AddToIcon from "../icons/AddToIcon";

function HorseOverview() {

    const [horses, setHorses] = useState<HorseModel[]>()
    const [stockItems, setStockItems] = useState<StockItemModel[]>([])
    const [successMessage, setSuccessMessage] = useState<string>()
    const [idToDelete, setIdToDelete] = useState<string>("")
    const [horseToEdit, setHorseToEdit] = useState<HorseModel>(
        {id: "", name: "", owner: {id: "", name: "", ownsHorse: []}, consumptionList: []})
    const [openModal, setOpenModal] = useState<"add" | "edit" | "delete" | "addConsumption">()

    const openAddModal = () => {
        setOpenModal("add")
        setSuccessMessage("")
    }
    const openDeleteModal = (id: string) => {
        setOpenModal("delete")
        setIdToDelete(id)
        setSuccessMessage("")
    }
    const openEditModal = (horseToEdit: any) => {
        setOpenModal("edit")
        setHorseToEdit(horseToEdit)
        setSuccessMessage("")
    }
    const openAddConsumptionModal = (horseToEdit: HorseModel) => {
        setOpenModal("addConsumption")
        setHorseToEdit(horseToEdit)
        setSuccessMessage("")
    }
    const closeModal = () => {
        setOpenModal(undefined)
    }
    const getAllStockItems = () => {
        axios.get("/stock/")
            .then((response) => response.data)
            .catch((error) => console.error("Error while getting Stockitems:" + error))
            .then(setStockItems)
    }

    const getAllHorses = () => {
        axios.get("/horses/")
            .then((response) => response.data)
            .catch((error) => console.error("Error while getting Horses:" + error))
            .then(setHorses)
    }

    const removeConsumption = (consumptionItemToDeleteId: string, editedHorse: HorseModel) => {
        editedHorse.consumptionList = editedHorse.consumptionList.filter(consumptionItem => consumptionItem.id !== consumptionItemToDeleteId)
        axios.put("/horses/", editedHorse)
            .catch((e) => console.error("PUT Error: " + e))
            .then(getAllHorses)
            .then(() => setSuccessMessage("Eintrag erfolgreich gelöscht"))
    }

    useEffect(() => {
        getAllHorses()
        getAllStockItems()
    }, [])

    if (horses === undefined) {
        return <BounceLoader
            size={100}
            aria-label="Loading Spinner"
            data-testid="loader"
            color="#36d7b7"
            cssOverride={{
                margin: "0 auto"
            }}
        />
    }

    return (
        <>
            <AddHorseModal modalIsOpen={openModal === "add"}
                           closeModal={closeModal}
                           reloadHorses={getAllHorses}
                           setSuccessMessage={setSuccessMessage}/>
            <DeleteHorseModal modalIsOpen={openModal === "delete"}
                              closeModal={closeModal}
                              reloadHorses={getAllHorses}
                              setSuccessMessage={setSuccessMessage}
                              idToDelete={idToDelete}/>
            <EditHorseModal modalIsOpen={openModal === "edit"}
                            closeModal={closeModal}
                            reloadHorses={getAllHorses}
                            setSuccessMessage={setSuccessMessage}
                            horseToEdit={horseToEdit}/>
            <AddConsumptionModal modalIsOpen={openModal === "addConsumption"}
                                 closeModal={closeModal}
                                 reloadHorses={getAllHorses}
                                 stockItemList={stockItems}
                                 setSuccessMessage={setSuccessMessage}
                                 selectedHorse={horseToEdit}/>
            {horses.length > 0 ?
                <>
                    <div className={"overview-table-wrapper"}>
                        <table>
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Besitzer</th>
                                <th>Täglicher Verbrauch</th>
                                <th>Aktionen</th>
                            </tr>
                            </thead>
                            <tbody>
                            {horses.map(horse => {
                                return <tr key={horse.id}>
                                    <td><strong>{horse.name}</strong></td>
                                    <td>{horse.owner.name}</td>
                                    <td>
                                        <div className={"consumption-cell"}>
                                            {horse.consumptionList.length > 0 ? horse.consumptionList
                                                    .map(consumptionObject => {
                                                        return <p
                                                            key={consumptionObject.id}>{consumptionObject.name}: {consumptionObject.dailyConsumption}
                                                            <abbr title={"Kilogramm"}>kg</abbr>
                                                            <DeleteIcon idToDelete={""}
                                                                        onClickAction={() => removeConsumption(consumptionObject.id, horse)}></DeleteIcon>
                                                            <br/>
                                                        </p>
                                                    })
                                                : <p>Keine Verbräuche angelegt</p>}

                                            <AddToIcon onClickAction={openAddConsumptionModal}
                                                       title={"Neuen Eintrag hinzufügen"}
                                                       addTo={horse}/>
                                        </div>
                                    </td>
                                    <td>
                                        <div className={"action-cell"}>
                                            <EditIcon onClickAction={openEditModal}
                                                      itemToEdit={horse}/>
                                            <DeleteIcon onClickAction={openDeleteModal}
                                                        idToDelete={horse.id}/>
                                        </div>
                                    </td>
                                </tr>
                            })}
                            </tbody>
                        </table>
                    </div>
                    {successMessage && <div className={"success-message"}>{successMessage}</div>}
                </>
                :
                <div>
                    {successMessage && <div className={"success-message"}>{successMessage}</div>}
                    <p>Keine Pferde im Stall</p>
                </div>
            }
            <AddIcon openModal={openAddModal} title={"Neuen Verbrauch hinzufügen"}/>
        </>
    );
}

export default HorseOverview;
