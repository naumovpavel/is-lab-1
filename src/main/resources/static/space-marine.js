$(document).ready(function () {
    const socket = new SockJS('/websocket');
    const stompClient = Stomp.over(socket);
    const urlParams = new URLSearchParams(window.location.search);
    const currentPage = parseInt($('#last-page').text());
    let offset = 0;
    const pageSize = parseInt(urlParams.get('size') || 10);
    const sortBy = urlParams.get('sortBy') || 'name';
    const sortDirection = urlParams.get('sortDirection') || 'asc';
    const searchValue = urlParams.get('searchValue') || '';
    const columnName = urlParams.get('columnName') || '';

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/space-marines', function (message) {
            const data = JSON.parse(message.body);
            handleMarineEvent(data);
        });
    });

    function handleMarineEvent(data) {
        if (data.id) {
            updateOrCreateMarineRow(data);
        } else {
            offset--;
            removeMarineRow(data.deleteID);
        }
        $('#last-page').text(currentPage + Math.floor(offset / pageSize));
    }

    function updateOrCreateMarineRow(marine) {
        let $row = $(`#${marine.id}`);
        if ($row.length) {
            $row.remove();
        } else {
            offset++;
        }
        update(marine);
    }

    function removeMarineRow(marineId) {
        $(`#${marineId}`).remove();
        const rows = $('tbody tr').get();
        if (rows.length < pageSize) {
            location.reload(true);
        }
    }

    function filterRow(row) {
        if (!columnName) return true;
        return $(row).find(`td:nth-child(${getColumnIndex(columnName)})`).text().includes(searchValue);
    }

    function update(marine) {
        const rows = $('tbody tr').get();

        const newRow = `
            <tr id="${marine.id}">
                <td>${marine.name}</td>
                <td>${marine.health || ''}</td>
                <td>${marine.coordinates.x || ''}</td>
                <td>${marine.coordinates.y || ''}</td>
                <td>${marine.chapter.name || ''}</td>
                <td>${marine.owner.username || ''}</td>
                <td>
                    <a href="/space-marines/edit/${marine.id}">Редактировать</a>
                    <form action="/space-marines/delete/${marine.id}" method="post" style="display:inline;">
                        <button type="submit">Удалить</button>
                    </form>
                </td>
            </tr>
        `;

        if (filterRow($(newRow)[0])) {
            rows.push($(newRow)[0]);
        }

        rows.sort(function (a, b) {
            let valA = $(a).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();
            let valB = $(b).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();

            if (getColumnIndex(sortBy) === 3 || getColumnIndex(sortBy) === 4) {
                valA = parseFloat(valA) || 0;
                valB = parseFloat(valB) || 0;
            }

            if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
            if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        if (rows.length < pageSize) {
            if(rows.length == 0 && page > 1) {
                urlParams.set('page') = page - 1;
            }
            location.reload(true);
        }

        if (rows.length > pageSize) {
            rows.pop();
        }

        $('tbody').empty();

        rows.forEach(row => {
            $('tbody').append(row);
        });

    }

    function getColumnIndex(field) {
        const columnMap = {
            name: 1,
            health: 2,
            'coordinates.x': 3,
            'coordinates.y': 4,
            'chapter.name': 5,
            'chapter.owner': 6
        };
        return columnMap[field] || 1;
    }
});