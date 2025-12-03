// server/include/domain/InventoryRecord.h
#pragma once

struct InventoryRecord
{
    int materialId;
    int quantity;

    InventoryRecord():materialId(0),quantity(0){}
};
